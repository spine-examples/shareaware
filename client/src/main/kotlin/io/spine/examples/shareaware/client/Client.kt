/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.shareaware.client

import com.google.protobuf.Message
import io.grpc.ManagedChannelBuilder
import io.spine.base.CommandMessage
import io.spine.base.EntityState
import io.spine.base.EventMessage
import io.spine.client.Client
import io.spine.client.ClientRequest
import io.spine.client.EventFilter
import io.spine.client.EventFilter.eq
import io.spine.client.Subscription
import io.spine.core.UserId
import io.spine.examples.shareaware.WalletId
import io.spine.examples.shareaware.client.ProtoExtensions.buildWithId
import io.spine.examples.shareaware.client.ProtoExtensions.buildWithOwner
import io.spine.examples.shareaware.client.ProtoExtensions.toUserId
import io.spine.examples.shareaware.wallet.command.CreateWallet
import io.spine.examples.shareaware.wallet.event.WalletCreated
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interacts with the app server via gRPC.
 */
public class DesktopClient private constructor(
    host: String,
    port: Int
) {

    public companion object {

        @Volatile
        private var instance: DesktopClient? = null

        public fun init(host: String, port: Int): DesktopClient =
            instance ?: synchronized(this) {
                instance ?: DesktopClient(host, port).also { instance = it }
            }
    }

    private val client: Client
    private lateinit var user: UserId
    private lateinit var walletId: WalletId

    init {
        val channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        client = Client
            .usingChannel(channel)
            .build()
        authenticateUser()
    }

    /**
     * Sends command to authenticate user in the system.
     *
     * A command `CreateWallet` is used for user creation because ShareAware server
     * does not provide neither user registration nor authorisation features at the moment.
     */
    private fun authenticateUser() {
        val userId = UUID
            .randomUUID()
            .toUserId()
        val createWalletOperation = CreateWalletOperation(client, userId)
        this.user = userId
        this.walletId = createWalletOperation.execute()
    }

    /**
     * Sends a command to the server on behalf of the authenticated user.
     */
    public fun command(message: CommandMessage) {
        clientRequest()
            .command(message)
            .postAndForget()
    }

    /**
     * Subscribes to the changes in entity of the given type.
     *
     * @param type type of the entity on which changes subscription works
     * @param id entity ID by which arrived entities will be filtered
     * @param observer callback function that will be triggered when the entity state changes
     */
    public fun <S : EntityState> subscribeToEntity(
        type: Class<S>,
        id: Message,
        observer: (S) -> Unit
    ) {
        clientRequest()
            .subscribeTo(type)
            .byId(id)
            .observe(observer)
            .post()
    }

    /**
     * Subscribes to the events of the provided type.
     *
     * @param type type of the event to subscribe on
     * @param filter filter by which arrived events of the provided type will be filtered
     * @param observer callback function that will be triggered when the event arrives
     * @return the subscription on the event for a possibility to cancel it
     */
    public fun <E : EventMessage> subscribeToEvent(
        type: Class<E>,
        filter: EventFilter,
        observer: (E) -> Unit
    ): Subscription {
        val subscription = clientRequest()
            .subscribeToEvent(type)
            .where(filter)
            .observe { event ->
                observer(event)
            }
            .post()
        return subscription
    }

    /**
     * Subscribes to the event of the provided type
     * and cancels itself after the observer has worked.
     *
     * @param type type of the event to subscribe on
     * @param filter filter by which arrived events of the provided type will be filtered
     * @param observer callback function that will be triggered when the event arrives
     */
    public fun <E : EventMessage> subscribeOnce(
        type: Class<E>,
        filter: EventFilter,
        observer: (E) -> Unit
    ) {
        var subscription: Subscription? = null
        subscription = subscribeToEvent(
            type,
            filter
        ) {
            observer(it)
            client.subscriptions()
                .cancel(subscription!!)
        }
    }

    /**
     * Retrieves the entity with a provided type and ID.
     *
     * @param type type of the entity to be retrieved
     * @param id entity ID by which the query result will be filtered
     * @return the retrieved entity with provided ID and type if it exists, `null` otherwise.
     */
    public fun <E : EntityState> readEntity(type: Class<E>, id: Message): E? {
        val entities = clientRequest()
            .select(type)
            .byId(id)
            .run()
        if (entities.isEmpty()) {
            return null
        }
        return entities[0]
    }

    /**
     * Returns the ID of the authenticated user.
     */
    public fun authenticatedUser(): UserId {
        return user
    }

    /**
     * Returns the ID of the user's wallet.
     */
    public fun wallet(): WalletId {
        return walletId
    }

    /**
     * Returns the client request to the server
     * on behalf of the authenticated user.
     */
    private fun clientRequest(): ClientRequest {
        return client.onBehalfOf(user)
    }
}

/**
 * Operation that creates the wallet for the user.
 */
private class CreateWalletOperation(
    private val client: Client,
    private val user: UserId
) {

    /**
     * Creates a wallet for the user.
     *
     * @return the ID of the created wallet
     */
    fun execute(): WalletId {
        val walletId = WalletId
            .newBuilder()
            .buildWithOwner(user)
        val createWallet = CreateWallet
            .newBuilder()
            .buildWithId(walletId)
        val walletCreated = subscribeToWalletCreated(walletId)
        client
            .asGuest()
            .command(createWallet)
            .postAndForget()
        return walletCreated.get().wallet
    }

    /**
     * Subscribes to the `WalletCreated` event.
     *
     * @param id the ID of the wallet that needs to be created
     * @return the result of the subscription with nested `WalletCreated` event
     */
    private fun subscribeToWalletCreated(id: WalletId): CompletableFuture<WalletCreated> {
        val walletField = WalletCreated.Field.wallet()
        val walletCreated: CompletableFuture<WalletCreated> = CompletableFuture()
        var subscription: Subscription? = null
        subscription = client
            .asGuest()
            .subscribeToEvent(WalletCreated::class.java)
            .where(eq(walletField, id))
            .observe { event ->
                walletCreated.complete(event)
                client.subscriptions()
                    .cancel(subscription!!)
            }
            .post()
        return walletCreated
    }
}

/**
 * Extensions for the `Proto` types used in the `DesktopClient`.
 */
private object ProtoExtensions {

    /**
     * Returns the `CreateWallet` command.
     *
     * @param id the ID of the wallet to create
     */
    fun CreateWallet.Builder.buildWithId(id: WalletId): CreateWallet {
        return this
            .setWallet(id)
            .vBuild()
    }

    /**
     * Returns a `UserId` taking a generated UUID value as a user identifier.
     */
    fun UUID.toUserId(): UserId {
        return UserId
            .newBuilder()
            .setValue(this.toString())
            .vBuild()
    }

    /**
     * Returns a `WalletId` taking a `UserId` as a value for a wallet identifier.
     */
    fun WalletId.Builder.buildWithOwner(id: UserId): WalletId {
        return this
            .setOwner(id)
            .vBuild()
    }
}

/**
 * Subscription for the `EntityState` changes.
 *
 * Provides an optional callback function that allows access
 * to the previous state of the observed entity.
 *
 * @param entityType type of the entity on which changes subscription works
 * @param client client with the help of which the entity will be subscribed
 * @param id entity ID by which arrived entities will be filtered
 * @param previousStateAccessor the callback function that provides access to the state of the entity
 * before the updated one has arrived
 */
public class EntitySubscription<S : EntityState> internal constructor(
    entityType: Class<S>,
    client: DesktopClient,
    id: Message,
    previousStateAccessor: (S?) -> Unit = {}
) {
    private var state: MutableStateFlow<S?>

    init {
        val entity = client.readEntity(entityType, id)
        state = MutableStateFlow(entity)
        client.subscribeToEntity(entityType, id) { value ->
            previousStateAccessor(state.value)
            state.value = value
        }
    }

    /**
     * Provides the current state of the subscribed entity.
     */
    public fun state(): StateFlow<S?> {
        return state
    }
}

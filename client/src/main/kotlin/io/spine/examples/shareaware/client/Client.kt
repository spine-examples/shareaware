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
import io.spine.core.UserId
import io.spine.examples.shareaware.WalletId
import io.spine.util.Exceptions

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
    private var user: UserId? = null
    private var wallet: WalletId? = null

    init {
        val channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        client = Client
            .usingChannel(channel)
            .build()
    }

    /**
     * Sends a command to the server.
     */
    public fun command(message: CommandMessage) {
        clientRequest()
            .command(message)
            .postAndForget()
    }

    /**
     * Subscribes to the changes in entities of the given type.
     *
     * @param type type of the entity on which changes subscription works
     * @param observer callback function that will be triggered when the entity state changes
     */
    public fun <S : EntityState> subscribeToEntity(type: Class<S>, observer: (S) -> Unit) {
        clientRequest()
            .subscribeTo(type)
            .observe(observer)
            .post()
    }

    /**
     * Subscribes to the events of the provided type.
     *
     * @param type type of the event to subscribe on
     * @param observer callback function that will be triggered when the event arrives
     */
    public fun <E : EventMessage> subscribeToEvent(type: Class<E>, observer: (E) -> Unit) {
        clientRequest()
            .subscribeToEvent(type)
            .observe(observer)
            .post()
    }

    /**
     * Retrieves the entity with a provided type and ID.
     *
     * @param type type of the entity to be retrieved
     * @param id entity ID by which the query result will be filtered
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
     * Returns the client request to the server
     * on behalf of the authenticated user if it exists
     * otherwise on behalf of the guest.
     */
    private fun clientRequest(): ClientRequest {
        if (user == null) {
            return client.asGuest()
        }
        return client.onBehalfOf(user)
    }

    /**
     * Configures the `DesktopClient` with authenticated user,
     * after it set all actions will occur on behalf of the user.
     */
    public fun authenticatedUser(id: UserId) {
        this.user = id
        this.wallet = WalletId
            .newBuilder()
            .setOwner(id)
            .vBuild()
    }

    /**
     * Returns the ID of the authenticated user if it exists.
     *
     * @throws IllegalStateException when the authenticated user is not configured for the client.
     */
    public fun authenticatedUser(): UserId? {
        if (user != null) {
            return user
        }
        throw Exceptions.newIllegalStateException(
            "There is no authenticated user configured for the client."
        )
    }

    /**
     * Returns the ID of the user's wallet if it exists.
     *
     * @throws IllegalStateException when the user's wallet does not exist
     * because the authenticated user is not configured to the client.
     */
    public fun wallet(): WalletId? {
        if (wallet != null) {
            return wallet
        }
        throw Exceptions.newIllegalStateException(
            "There is no user's wallet ID because of the " +
                    "authenticated user is not configured for the client."
        )
    }
}

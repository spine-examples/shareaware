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
import io.spine.core.UserId
import io.spine.examples.shareaware.WalletId

/**
 * Interacts with the app server via gRPC.
 */
public class DesktopClient private constructor(
    private val user: UserId,
    host: String,
    port: Int
) {

    public companion object {

        @Volatile
        private var instance: DesktopClient? = null

        public fun init(userId: UserId, host: String, port: Int): DesktopClient =
            instance ?: synchronized(this) {
                instance ?: DesktopClient(userId, host, port).also { instance = it }
            }
    }

    private val client: Client
    private val wallet: WalletId

    init {
        val channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build()
        client = Client
            .usingChannel(channel)
            .build()
        wallet = WalletId
            .newBuilder()
            .setOwner(user)
            .vBuild()
    }

    /**
     * Sends a command to the server.
     */
    public fun command(message: CommandMessage) {
        client
            .onBehalfOf(user)
            .command(message)
            .postAndForget()
    }

    /**
     * Subscribes the `observer` to the entity with a provided `type`.
     */
    public fun <S : EntityState> subscribeToEntity(type: Class<S>, observer: (S) -> Unit) {
        client
            .onBehalfOf(user)
            .subscribeTo(type)
            .observe(observer)
            .post()
    }

    /**
     * Subscribes the `observer` to the event with a provided `type`.
     */
    public fun <E : EventMessage> subscribeToEvent(type: Class<E>, observer: (E) -> Unit) {
        client
            .onBehalfOf(user)
            .subscribeToEvent(type)
            .observe(observer)
            .post()
    }

    /**
     * Retrieves the entity with a provided type and ID.
     */
    public fun <E : EntityState> readEntity(type: Class<E>, id: Message): E? {
        val entities = client
            .onBehalfOf(user)
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
    public fun user(): UserId {
        return user
    }

    /**
     * Returns the ID of the user's wallet.
     */
    public fun wallet(): WalletId {
        return wallet
    }
}

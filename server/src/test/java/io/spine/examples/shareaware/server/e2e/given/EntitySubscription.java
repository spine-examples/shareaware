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

package io.spine.examples.shareaware.server.e2e.given;

import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.client.Client;
import io.spine.core.UserId;
import io.spine.examples.shareaware.testing.server.e2e.AsyncObserver;
import io.spine.examples.shareaware.testing.server.e2e.StateRouter;

import java.util.function.Consumer;

/**
 * Configures the {@code AsyncObserver} with how to send a command
 * and how to observe the entity state changes using {@code Spine} Client API.
 */
class EntitySubscription<S extends EntityState> extends AsyncObserver<S, CommandMessage> {

    EntitySubscription(Class<S> entityType, Client client, UserId user) {
        super(subscribeAndObserve(entityType, client, user), command(client, user));
    }

    /**
     * Returns callback that defines how to observe an entity state using {@code Spine} Client API.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // It's fine as this callback calls once in the constructor.
    private static
    <S extends EntityState> StateRouter<S> subscribeAndObserve(Class<S> entityType, Client client, UserId user) {
        return recipient -> client.onBehalfOf(user)
                                       .subscribeTo(entityType)
                                       .observe(recipient)
                                       .post();
    }

    /**
     * Returns callback which defines how to send a command using {@code Spine} Client API.
     */
    private static Consumer<CommandMessage> command(Client client, UserId user) {
        return commandMessage -> client.onBehalfOf(user)
                                       .command(commandMessage)
                                       .postAndForget();
    }
}

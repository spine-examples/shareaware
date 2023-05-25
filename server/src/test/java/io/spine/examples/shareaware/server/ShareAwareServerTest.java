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

package io.spine.examples.shareaware.server;

import io.spine.client.QueryResponse;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.server.Server;
import io.spine.testing.client.grpc.TestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.examples.shareaware.server.given.GivenWallet.walletId;
import static io.spine.testing.core.given.GivenUserId.newUuid;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;

@DisplayName("ShareAware server should")
final class ShareAwareServerTest {

    private static final String HOST = "localhost";
    private Server server;
    private TestClient client;
    private UserId user;

    @BeforeEach
    void setup() {
        server = ShareAwareServer.create();
        user = newUuid();
        client = new TestClient(user, HOST, DEFAULT_CLIENT_SERVICE_PORT);
        try {
            server.start();
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        client.shutdown();
        server.shutdownAndWait();
    }

    @Test
    @DisplayName("accept command and create `WalletBalance` projection in response")
    void command() {
        WalletId walletId = walletId(user);
        client.post(createWallet(walletId));

        QueryResponse wallets = client.queryAll(WalletBalance.class);
        assertThat(wallets.getMessageCount()).isEqualTo(1);
    }
}

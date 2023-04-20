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

package io.spine.examples.shareaware.server.e2e;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.spine.client.Client;
import io.spine.core.UserId;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.market.MarketDataProvider;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.server.Server;
import io.spine.testing.core.given.GivenUserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.grpc.Status.CANCELLED;
import static io.spine.client.Client.usingChannel;
import static io.spine.examples.shareaware.given.GivenMoney.zero;
import static io.spine.examples.shareaware.server.e2e.given.OneTimeVisitorTestEnv.*;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.examples.shareaware.server.given.GivenWallet.walletId;
import static io.spine.server.Server.atPort;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.fail;

public class OneTimeVisitorTest {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4242;

    private Client client;
    private Server server;
    private ManagedChannel channel;

    private final MarketDataProvider provider = MarketDataProvider.instance();

    @BeforeEach
    void startAndConnect() throws IOException {
        channel = forAddress(ADDRESS, PORT)
                .usePlaintext()
                .build();
        server = atPort(PORT)
                .add(TradingContext.newBuilder())
                .build();
        server.start();
        client = usingChannel(channel).build();
        provider.runWith(Duration.ofSeconds(1));
    }

    @AfterEach
    void stopAndDisconnect() throws InterruptedException {
        try {
            client.close();
        } catch (StatusRuntimeException e) {
            if (e.getStatus()
                 .equals(CANCELLED)) {
                fail(e);
            }
        }
        server.shutdown();
        channel.shutdown();
        channel.awaitTermination(1, SECONDS);
        provider.stop();
    }

    @Test
    void oneTimeVisit() {
        UserId user = GivenUserId.generated();
        WalletId walletId = walletId(user);
        AtomicReference<List<Share>> shares = new AtomicReference<>();
        client.onBehalfOf(user)
              .subscribeTo(WalletBalance.class)
              .observe((state) -> {
                  assertThat(state.getBalance()).isEqualTo(zero());
              })
              .subscribeToEvent(WalletCreated.class)
              .observe((event) -> {
                  assertThat(event).isEqualTo(walletCreatedWith(walletId));
              })
              .subscribeTo(AvailableMarketShares.class)
              .observe((projection) -> {
                  shares.set(projection.getShareList());
              })
              .post();

        sleepUninterruptibly(ofMillis(1500));

        client.onBehalfOf(user)
              .command(createWallet(walletId))
              .postAndForget();
    }
}

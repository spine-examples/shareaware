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

import com.google.common.collect.ImmutableList;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.client.Client;
import io.spine.core.UserId;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.e2e.given.OneTimeVisitorTestEnv;
import io.spine.examples.shareaware.server.market.MarketDataProvider;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.server.Server;
import io.spine.testing.core.given.GivenUserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.grpc.Status.CANCELLED;
import static io.spine.client.Client.usingChannel;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.OneTimeVisitorTestEnv.*;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.examples.shareaware.server.given.GivenWallet.walletId;
import static io.spine.server.Server.atPort;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
class OneTimeVisitorTest {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4242;
    private Client client;
    private Server server;
    private ManagedChannel channel;
    private static final MarketDataProvider provider = MarketDataProvider.instance();
    private final UserId user = GivenUserId.generated();
    private final WalletId walletId = walletId(user);

    @BeforeAll
    static void startProvider() {
        provider.runWith(Duration.ofSeconds(1));
    }

    @AfterAll
    static void stopProvider() {
        provider.stop();
    }

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
    }

    @Test
    void oneTimeVisit() throws ExecutionException, InterruptedException {
        sleepUninterruptibly(ofMillis(1500));
        createWalletForUser();
        tryToPurchaseTeslaShare();
        replenishWallet();
        purchaseTeslaShare();
        withdrawAllMoney();
    }

    private void createWalletForUser() throws ExecutionException, InterruptedException {
        CreateWallet createWallet = createWallet(walletId);
        WalletBalance zeroBalance = zeroWalletBalance(walletId);

        CompletableFuture<WalletCreated> actualWalletCreated =
                subscribeToEvent(WalletCreated.class);
        CompletableFuture<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class);
        command(createWallet);

        assertThat(actualBalance.get()).isEqualTo(zeroBalance);
        assertThat(actualWalletCreated.get()).isEqualTo(walletCreatedWith(walletId));
        client.subscriptions()
              .cancelAll();
    }

    private void tryToPurchaseTeslaShare() throws ExecutionException,
                                                  InterruptedException {
        PurchaseShares purchaseTeslaShare = purchaseTeslaShareFor(user, shares().get());
        InsufficientFunds expectedInsufficientFunds = insufficientFundsAfter(purchaseTeslaShare);

        CompletableFuture<InsufficientFunds> actualInsufficientFunds =
                subscribeToEvent(InsufficientFunds.class);
        command(purchaseTeslaShare);

        assertThat(actualInsufficientFunds.get()).isEqualTo(expectedInsufficientFunds);
        client.subscriptions()
              .cancelAll();
    }

    private void replenishWallet() throws ExecutionException, InterruptedException {
        ReplenishWallet replenishWallet = OneTimeVisitorTestEnv.replenishWallet(walletId, usd(500));
        WalletBalance expectedBalance = walletBalanceAfterReplenishment(replenishWallet);
        WalletReplenished expectedWalletReplenished = walletReplenishedAfter(replenishWallet);

        CompletableFuture<WalletReplenished> actualWalletReplenished =
                subscribeToEvent(WalletReplenished.class);
        CompletableFuture<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class);
        command(replenishWallet);

        assertThat(actualWalletReplenished.get())
                .isEqualTo(expectedWalletReplenished);
        assertThat(actualBalance.get()).isEqualTo(expectedBalance);
        client.subscriptions()
              .cancelAll();
    }

    private void purchaseTeslaShare() throws ExecutionException,
                                             InterruptedException {
        WalletBalance currentBalance = walletBalance();
        PurchaseShares purchaseShares = purchaseTeslaShareFor(user, shares().get());
        SharesPurchased expectedSharesPurchased = sharesPurchasedAfter(purchaseShares);
        WalletBalance expectedBalance = balanceAfterTeslaPurchase(purchaseShares,
                                                                  currentBalance);
        InvestmentView expectedInvestment = investmentAfterPurchase(purchaseShares);

        CompletableFuture<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class);
        CompletableFuture<SharesPurchased> actualSharesPurchased =
                subscribeToEvent(SharesPurchased.class);
        CompletableFuture<InvestmentView> actualInvestment =
                subscribeToState(InvestmentView.class);
        command(purchaseShares);

        assertThat(actualSharesPurchased.get()).isEqualTo(expectedSharesPurchased);
        assertThat(actualBalance.get()).isEqualTo(expectedBalance);
        assertThat(actualInvestment.get()).isEqualTo(expectedInvestment);
        client.subscriptions()
              .cancelAll();
    }

    void withdrawAllMoney() throws ExecutionException,
                                   InterruptedException {
        WalletBalance currentBalance = walletBalance();
        WithdrawMoney withdrawAllMoney = OneTimeVisitorTestEnv.withdrawAllMoney(
                currentBalance, walletId);
        MoneyWithdrawn expectedMoneyWithdrawn = moneyWithdrawnAfter(withdrawAllMoney,
                                                                    currentBalance);

        CompletableFuture<MoneyWithdrawn> actualWithdrawnMoney =
                subscribeToEvent(MoneyWithdrawn.class);
        CompletableFuture<WalletBalance> balanceAfterWithdrawn =
                subscribeToState(WalletBalance.class);
        command(withdrawAllMoney);

        assertThat(actualWithdrawnMoney.get()).isEqualTo(expectedMoneyWithdrawn);
        assertThat(balanceAfterWithdrawn.get()).isEqualTo(zeroWalletBalance(walletId));
        client.subscriptions()
              .cancelAll();
    }

    private <E extends EventMessage> CompletableFuture<E> subscribeToEvent(Class<E> type) {
        CompletableFuture<E> future = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeToEvent(type)
              .observe(future::complete)
              .post();
        return future;
    }

    private <S extends EntityState> CompletableFuture<S> subscribeToState(Class<S> type) {
        CompletableFuture<S> future = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeTo(type)
              .observe(future::complete)
              .post();
        return future;
    }

    private void command(CommandMessage commandMessage) {
        client.onBehalfOf(user)
              .command(commandMessage)
              .postAndForget();
    }

    private WalletBalance walletBalance() {
        ImmutableList<WalletBalance> balances = client
                .onBehalfOf(user)
                .select(WalletBalance.class)
                .run();
        if (balances.size() != 1) {
            fail();
        }
        return balances.get(0);
    }

    private CompletableFuture<List<Share>> shares() {
        CompletableFuture<List<Share>> shares = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeTo(AvailableMarketShares.class)
              .observe(projection -> shares.complete(projection.getShareList()))
              .post();
        return shares;
    }
}

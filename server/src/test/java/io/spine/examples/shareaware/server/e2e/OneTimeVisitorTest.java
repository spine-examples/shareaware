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
import io.spine.money.Money;
import io.spine.server.Server;
import io.spine.testing.core.given.GivenUserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class OneTimeVisitorTest {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4242;
    private Client client;
    private Server server;
    private ManagedChannel channel;
    private static final MarketDataProvider provider = MarketDataProvider.instance();

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
        UserId user = GivenUserId.generated();
        WalletId walletId = walletId(user);
        List<Share> shares = shares(user).get();
        Share tesla = tesla(shares);

        WalletBalance balanceAfterCreation = createWalletFor(user);
        WalletBalance zeroBalance = zeroWalletBalance(walletId);
        assertThat(balanceAfterCreation).isEqualTo(zeroBalance);

        WalletBalance walletAfterFailedPurchase = tryToPurchaseTeslaShareFor(user, tesla);
        assertThat(walletAfterFailedPurchase).isEqualTo(balanceAfterCreation);

        Money replenishmentAmount = usd(500);
        WalletBalance balanceAfterReplenishment = replenishWalletFor(replenishmentAmount, walletId);
        WalletBalance expectedBalanceAfterReplenishment = walletBalanceWith(replenishmentAmount,
                                                                            walletId);
        assertThat(balanceAfterReplenishment).isEqualTo(expectedBalanceAfterReplenishment);

        InvestmentView investmentView = purchaseTeslaShareFor(user, tesla);
        InvestmentView expectedInvestmentView = investmentAfterTeslaPurchase(tesla, user);
        WalletBalance balanceAfterPurchase = walletBalance(user);
        WalletBalance expectedBalanceAfterPurchase =
                balanceAfterTeslaPurchase(tesla.getPrice(), balanceAfterReplenishment);
        assertThat(investmentView).isEqualTo(expectedInvestmentView);
        assertThat(balanceAfterPurchase).isEqualTo(expectedBalanceAfterPurchase);

        WalletBalance walletAfterWithdrawal = withdrawAllMoneyFrom(walletId);
        assertThat(walletAfterWithdrawal).isEqualTo(zeroBalance);
    }

    private WalletBalance createWalletFor(UserId user)
            throws ExecutionException, InterruptedException {
        WalletId walletId = walletId(user);
        CreateWallet createWallet = createWallet(walletId);

        CompletableFuture<WalletCreated> actualWalletCreated =
                subscribeToEvent(WalletCreated.class, user);
        CompletableFuture<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class, user);
        command(createWallet, user);

        assertThat(actualWalletCreated.get()).isEqualTo(walletCreatedWith(walletId));
        client.subscriptions()
              .cancelAll();
        return actualBalance.get();
    }

    private WalletBalance tryToPurchaseTeslaShareFor(UserId user, Share tesla)
            throws ExecutionException, InterruptedException {
        PurchaseShares purchaseTeslaShare = OneTimeVisitorTestEnv.purchaseTeslaShareFor(user,
                                                                                        tesla);
        InsufficientFunds expectedInsufficientFunds = insufficientFundsAfter(purchaseTeslaShare);

        CompletableFuture<InsufficientFunds> actualInsufficientFunds =
                subscribeToEvent(InsufficientFunds.class, user);
        command(purchaseTeslaShare, user);

        assertThat(actualInsufficientFunds.get()).isEqualTo(expectedInsufficientFunds);
        client.subscriptions()
              .cancelAll();
        return walletBalance(user);
    }

    private WalletBalance replenishWalletFor(Money amount, WalletId walletId)
            throws ExecutionException, InterruptedException {
        ReplenishWallet replenishWallet = replenishWallet(walletId, amount);
        WalletReplenished expectedWalletReplenished = walletReplenishedAfter(replenishWallet);

        UserId user = walletId.getOwner();
        CompletableFuture<WalletReplenished> actualWalletReplenished =
                subscribeToEvent(WalletReplenished.class, user);
        CompletableFuture<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class, user);
        command(replenishWallet, user);

        assertThat(actualWalletReplenished.get())
                .isEqualTo(expectedWalletReplenished);
        client.subscriptions()
              .cancelAll();
        return actualBalance.get();
    }

    private InvestmentView purchaseTeslaShareFor(UserId user, Share tesla)
            throws ExecutionException, InterruptedException {
        PurchaseShares purchaseShares = OneTimeVisitorTestEnv.purchaseTeslaShareFor(user, tesla);
        SharesPurchased expectedSharesPurchased = sharesPurchasedAfter(purchaseShares);

        CompletableFuture<SharesPurchased> actualSharesPurchased =
                subscribeToEvent(SharesPurchased.class, user);
        CompletableFuture<InvestmentView> actualInvestment =
                subscribeToState(InvestmentView.class, user);
        command(purchaseShares, user);

        assertThat(actualSharesPurchased.get()).isEqualTo(expectedSharesPurchased);
        client.subscriptions()
              .cancelAll();
        return actualInvestment.get();
    }

    private WalletBalance withdrawAllMoneyFrom(WalletId wallet)
            throws ExecutionException, InterruptedException {
        UserId user = wallet.getOwner();
        WalletBalance currentBalance = walletBalance(user);
        WithdrawMoney withdrawAllMoney = OneTimeVisitorTestEnv.withdrawAllMoney(
                currentBalance, wallet);
        MoneyWithdrawn expectedMoneyWithdrawn = moneyWithdrawnAfter(withdrawAllMoney,
                                                                    currentBalance);

        CompletableFuture<MoneyWithdrawn> actualWithdrawnMoney =
                subscribeToEvent(MoneyWithdrawn.class, user);
        CompletableFuture<WalletBalance> balanceAfterWithdrawn =
                subscribeToState(WalletBalance.class, user);
        command(withdrawAllMoney, user);

        assertThat(actualWithdrawnMoney.get()).isEqualTo(expectedMoneyWithdrawn);
        client.subscriptions()
              .cancelAll();
        return balanceAfterWithdrawn.get();
    }

    private <E extends EventMessage> CompletableFuture<E> subscribeToEvent(Class<E> type,
                                                                           UserId user) {
        CompletableFuture<E> future = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeToEvent(type)
              .observe(future::complete)
              .post();
        return future;
    }

    private <S extends EntityState> CompletableFuture<S> subscribeToState(Class<S> type,
                                                                          UserId user) {
        CompletableFuture<S> future = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeTo(type)
              .observe(future::complete)
              .post();
        return future;
    }

    private void command(CommandMessage commandMessage, UserId user) {
        client.onBehalfOf(user)
              .command(commandMessage)
              .postAndForget();
    }

    private WalletBalance walletBalance(UserId user) {
        ImmutableList<WalletBalance> balances = client
                .onBehalfOf(user)
                .select(WalletBalance.class)
                .run();
        if (balances.size() != 1) {
            fail();
        }
        return balances.get(0);
    }

    private CompletableFuture<List<Share>> shares(UserId user) {
        CompletableFuture<List<Share>> shares = new CompletableFuture<>();
        client.onBehalfOf(user)
              .subscribeTo(AvailableMarketShares.class)
              .observe(projection -> shares.complete(projection.getShareList()))
              .post();
        return shares;
    }
}

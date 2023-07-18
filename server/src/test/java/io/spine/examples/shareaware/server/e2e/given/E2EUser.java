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

import io.grpc.ManagedChannel;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.base.KnownMessage;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.given.GivenWallet;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import io.spine.server.tuple.EitherOf2;
import io.spine.testing.core.given.GivenUserId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.client.Client.usingChannel;
import static io.spine.examples.shareaware.MoneyCalculator.isGreater;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.E2ECommands.purchaseSharesFor;
import static io.spine.examples.shareaware.server.e2e.given.E2ECommands.replenishWallet;
import static io.spine.examples.shareaware.server.e2e.given.E2ECommands.withdrawMoneyFrom;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.walletBalanceWith;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.zeroWalletBalance;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a user for end-to-end ShareAware application tests which interacts
 * with the server-side with the help of {@link Client}.
 */
public final class E2EUser {

    private final Client client;
    private final UserId userId;
    private final WalletId walletId;
    private final EntitySubscription<WalletBalance> wallet;
    private final EntitySubscription<InvestmentView> investment;
    private final EntitySubscription<AvailableMarketShares> availableMarketShares;

    public E2EUser(ManagedChannel channel) {
        this.client = usingChannel(channel).build();
        this.userId = GivenUserId.generated();
        this.walletId = GivenWallet.walletId(userId);
        wallet = new EntitySubscription<>(WalletBalance.class, client, userId);
        investment = new EntitySubscription<>(InvestmentView.class, client, userId);
        availableMarketShares =
                new EntitySubscription<>(AvailableMarketShares.class, client, userId);
        createWalletForUser();
    }

    /**
     * Allows user to post the provided command to the server.
     */
    public void post(CommandMessage command) {
        client.onBehalfOf(userId)
              .command(command)
              .postAndForget();
    }

    /**
     * Returns the ID of the user.
     */
    public UserId id() {
        return userId;
    }

    /**
     * Returns the ID of the user's wallet.
     */
    public WalletId walletId() {
        return walletId;
    }

    /**
     * Describes the user's action to look at available shares on the market.
     */
    public List<Share> looksAtAvailableShares() {
        var shares = availableMarketShares
                .state()
                .getShareList();
        return shares;
    }

    /**
     * Describes the user's action to look at the investment.
     */
    public InvestmentView looksAtInvestment() {
        return investment.state();
    }

    /**
     * Describes the user's action to replenish his wallet.
     */
    public WalletBalance replenishesWalletFor(Money amount) {
        var replenishWallet = replenishWallet(walletId, amount);

        var balanceAfterReplenishment = wallet.onceUpdatedAfter(replenishWallet);
        var expectedBalance = walletBalanceWith(usd(500), walletId);
        assertThat(balanceAfterReplenishment).isEqualTo(expectedBalance);
        return balanceAfterReplenishment;
    }

    /**
     * Describes the user's action to purchase shares.
     *
     * <p>Returns either {@code InsufficientFunds} in the case when the total cost of purchase
     * was more than the amount of money on balance, or the {@code WalletBalance}
     * if the purchase was successful.
     */
    public EitherOf2<WalletBalance, InsufficientFunds> purchase(Share share, int howMany) {
        var purchaseShares = purchaseSharesFor(id(), share, howMany);

        var walletBeforePurchase = wallet.state();
        if (isGreater(purchaseShares.totalCost(), walletBeforePurchase.getBalance())) {
            var subscriptionOutcome = subscribeToEvent(InsufficientFunds.class);
            post(purchaseShares);

            var insufficientFunds = retrieveValueFrom(subscriptionOutcome);
            return EitherOf2.withB(insufficientFunds);
        }
        WalletBalance walletAfterPurchase = wallet.onceUpdatedAfter(purchaseShares);
        return EitherOf2.withA(walletAfterPurchase);
    }

    /**
     * Describes the user's action to withdraw all money from the wallet.
     *
     * <p>As a result, the wallet balance should be zero.
     */
    public WalletBalance withdrawsAllMoney(WalletBalance balance) {
        var balanceAfterWithdrawal = withdrawsMoney(balance.getBalance());
        return balanceAfterWithdrawal;
    }

    /**
     * Describes the user's action to withdraw the exact amount of money from the wallet.
     */
    private WalletBalance withdrawsMoney(Money amount) {
        var withdrawMoney = withdrawMoneyFrom(walletId, amount);
        WalletBalance walletAfterWithdraw = wallet.onceUpdatedAfter(withdrawMoney);
        return walletAfterWithdraw;
    }

    /**
     * Subscribes the user to receive the event of the passed type.
     */
    private <S extends EventMessage> SubscriptionOutcome<S> subscribeToEvent(Class<S> type) {
        var future = new CompletableFuture<S>();
        var subscription = client
                .onBehalfOf(userId)
                .subscribeToEvent(type)
                .observe(future::complete)
                .post();
        return new SubscriptionOutcome<>(future, subscription);
    }

    /**
     * Retrieves value from {@code SubscriptionOutcome} and cancels the subscription.
     */
    private <S extends KnownMessage> S retrieveValueFrom(SubscriptionOutcome<S> changedState) {
        try {
            S value = changedState.future()
                                  .get(10, SECONDS);
            cancel(changedState.subscription());
            return value;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Cancels the passed subscription.
     */
    private void cancel(Subscription subscription) {
        client.subscriptions()
              .cancel(subscription);
    }

    private void createWalletForUser() {
        var createWallet = createWallet(walletId);
        var initialBalance = wallet.onceUpdatedAfter(createWallet);
        assertThat(initialBalance).isEqualTo(zeroWalletBalance(walletId));
    }
}

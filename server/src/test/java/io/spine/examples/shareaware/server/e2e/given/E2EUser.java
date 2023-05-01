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

import com.google.common.collect.ImmutableList;
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.given.GivenWallet;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.*;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.examples.shareaware.server.e2e.given.E2EUserTestEnv.purchaseSharesFor;
import static io.spine.examples.shareaware.server.e2e.given.E2EUserTestEnv.replenishWallet;
import static io.spine.examples.shareaware.server.e2e.given.E2EUserTestEnv.withdrawMoneyFrom;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Represents a user for end-to-end tests which interacts
 * with the server-side with the help of {@link Client}.
 *
 * <p>In some tests, it may be required to extend the API provided by this type.
 * Then, subclassing should be done in a nested type, residing inside
 * the target test class.
 */
public class E2EUser {

    private final Client client;
    private final UserId userId;
    private final WalletId walletId;

    public E2EUser(Client client) {
        this.client = client;
        this.userId = GivenUserId.generated();
        this.walletId = GivenWallet.walletId(userId);
    }

    /**
     * Allows user to send the provided command to the server.
     */
    public void command(CommandMessage command) {
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
     * Describes the user's action to wait for shares to update on the market.
     */
    public List<Share> waitsForSharesToUpdate() {
        sleepUninterruptibly(ofMillis(1500));
        List<Share> shares = looksAtShares();
        return shares;
    }

    /**
     * Describes the user's action to sign up in the application.
     */
    public WalletBalance signsUp() {
        CreateWallet createWallet = createWallet(walletId);

        SubscriptionOutcome<WalletBalance> initialBalance =
                subscribeToState(WalletBalance.class);
        command(createWallet);

        return retrieveValueFrom(initialBalance);
    }

    /**
     * Describes the user's action to replenish his wallet.
     */
    public WalletBalance replenishesWalletFor(Money amount) {
        ReplenishWallet replenishWallet = replenishWallet(walletId(), amount);

        SubscriptionOutcome<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class);
        command(replenishWallet);

        return retrieveValueFrom(actualBalance);
    }

    /**
     * Describes the user's action to purchase shares.
     */
    public InvestmentView purchasesShares(Share share, int quantity) {
        PurchaseShares purchaseShares = purchaseSharesFor(id(), share, quantity);

        SubscriptionOutcome<InvestmentView> actualInvestment =
                subscribeToState(InvestmentView.class);
        command(purchaseShares);

        return retrieveValueFrom(actualInvestment);
    }

    /**
     * Describes the user's action to withdraw the exact amount of money from the wallet.
     */
    public WalletBalance withdrawsMoney(Money amount) {
        WithdrawMoney withdrawMoney = withdrawMoneyFrom(walletId, amount);

        SubscriptionOutcome<WalletBalance> balanceAfterWithdrawal =
                subscribeToState(WalletBalance.class);
        command(withdrawMoney);

        return retrieveValueFrom(balanceAfterWithdrawal);
    }

    /**
     * Describes the user's action to look at his wallet balance.
     */
    public WalletBalance looksAtWalletBalance() {
        ImmutableList<WalletBalance> balances = lookAt(WalletBalance.class);
        assertThat(balances.size()).isEqualTo(1);
        return balances.get(0);
    }

    /**
     * Describes the user's action to look at the available shares on the market.
     */
    public List<Share> looksAtShares() {
        CompletableFuture<List<Share>> shares = new CompletableFuture<>();
        client.onBehalfOf(id())
              .subscribeTo(AvailableMarketShares.class)
              .observe(projection -> shares.complete(projection.getShareList()))
              .post();
        try {
            return shares.get(1500, MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * This method is a public alias for {@link #subscribeToState(Class)}.
     */
    public <S extends EntityState> SubscriptionOutcome<S> expectsChangesIn(Class<S> type) {
       return subscribeToState(type);
    }

    /**
     * This method is a public alias for {@link #retrieveValueFrom(SubscriptionOutcome)}.
     */
    public <S extends EntityState> S checksChangesIn(SubscriptionOutcome<S> changedState) {
        return retrieveValueFrom(changedState);
    }

    /**
     * Subscribes the user to receive the event of the passed type.
     *
     * <p>Returns only the {@link CompletableFuture} that stores the event
     * without {@link Subscription}.
     *
     * @see E2EUser#subscribeToEvent(Class)
     */
    protected <E extends EventMessage> CompletableFuture<E>
    subscribeToEventAndForget(Class<E> type) {
        SubscriptionOutcome<E> subscriptionOutcome = subscribeToEvent(type);
        return subscriptionOutcome.future();
    }

    /**
     * Subscribes the user on changes of the passed type of the {@code EntityState}.
     *
     * <p>Returns only the {@link CompletableFuture} that stores the {@code EntityState}
     * without {@link Subscription}.
     *
     * @see E2EUser#subscribeToState(Class)
     */
    protected <S extends EntityState> CompletableFuture<S>
    subscribeToStateAndForget(Class<S> type) {
        SubscriptionOutcome<S> subscriptionOutcome = subscribeToState(type);
        return subscriptionOutcome.future();
    }

    /**
     * Subscribes the user to receive the event of the passed type.
     */
    protected <S extends EventMessage> SubscriptionOutcome<S> subscribeToEvent(Class<S> type) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(userId)
                .subscribeToEvent(type)
                .observe(future::complete)
                .post();
        return new SubscriptionOutcome<>(future, subscription);
    }

    /**
     * Subscribes the user on changes of the passed type of the {@code EntityState}.
     */
    protected <S extends EntityState> SubscriptionOutcome<S> subscribeToState(Class<S> type) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(userId)
                .subscribeTo(type)
                .observe(future::complete)
                .post();
        return new SubscriptionOutcome<>(future, subscription);
    }

    /**
     * Retrieves value from {@code SubscriptionOutcome} and cancels the subscription.
     */
    protected <S extends EntityState> S retrieveValueFrom(SubscriptionOutcome<S> changedState) {
        try {
            cancel(changedState.subscription());
            return changedState.future()
                               .get(10, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Allows user to take a look at all the {@code EntityState}s with the provided type.
     */
    private <S extends EntityState> ImmutableList<S> lookAt(Class<S> type) {
        return client
                .onBehalfOf(userId)
                .select(type)
                .run();
    }

    /**
     * Cancels the passed subscription.
     */
    private void cancel(Subscription subscription) {
        client.subscriptions()
              .cancel(subscription);
    }
}

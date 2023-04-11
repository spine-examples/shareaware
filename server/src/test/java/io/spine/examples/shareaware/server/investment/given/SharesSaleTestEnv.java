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

package io.spine.examples.shareaware.server.investment.given;

import io.spine.core.UserId;
import io.spine.examples.shareaware.ReplenishmentOperationId;
import io.spine.examples.shareaware.SaleId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.Investment;
import io.spine.examples.shareaware.investment.SharesSale;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.command.SellShares;
import io.spine.examples.shareaware.investment.event.SharesSaleFailed;
import io.spine.examples.shareaware.investment.event.SharesSold;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.examples.shareaware.server.market.MarketProcess;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.money.Money;

import static io.spine.examples.shareaware.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.MoneyCalculator.sum;

public final class SharesSaleTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private SharesSaleTestEnv() {
    }

    public static SellShares sellShareAfter(PurchaseShares c) {
        return sellShares(c.getPurchaser(), c.getShare(), 1);
    }

    public static SellShares sellShareFrom(Investment investment) {
        UserId user = investment.getId()
                                .getOwner();
        ShareId share = investment.getId()
                                  .getShare();
        return sellShares(user, share, 1);
    }

    public static SellShares sellMoreSharesThanIn(Investment investment) {
        UserId user = investment.getId()
                                .getOwner();
        ShareId share = investment.getId()
                                  .getShare();
        return sellShares(user, share, investment.getSharesAvailable() + 1);
    }

    private static SellShares sellShares(UserId user,
                                         ShareId share,
                                         int quantity) {
        return SellShares
                .newBuilder()
                .setSaleProcess(SaleId.generate())
                .setSeller(user)
                .setShare(share)
                .setQuantity(quantity)
                .setPrice(GivenMoney.usd(20))
                .vBuild();
    }

    public static Wallet walletAfter(PurchaseShares purchaseCommand,
                                     SellShares saleCommand,
                                     Wallet wallet) {
        Money currentBalance = subtract(wallet.getBalance(), purchaseCommand.totalCost());
        return wallet
                .toBuilder()
                .setBalance(sum(currentBalance, saleCommand.totalCost()))
                .vBuild();
    }

    public static BalanceRecharged balanceRechargedBy(SellShares command, Wallet wallet) {
        Money currentBalance = sum(wallet.getBalance(), command.totalCost());
        return balanceRecharged(command, currentBalance);
    }

    public static BalanceRecharged balanceRechargedAfter(SellShares saleCommand,
                                                         PurchaseShares purchaseCommand,
                                                         Wallet wallet) {
        Money balanceAfterPurchase = subtract(wallet.getBalance(),
                                              purchaseCommand.totalCost());
        Money currentBalance = sum(balanceAfterPurchase, saleCommand.totalCost());
        return balanceRecharged(saleCommand, currentBalance);
    }

    private static BalanceRecharged balanceRecharged(SellShares command, Money balance) {
        return BalanceRecharged
                .newBuilder()
                .setWallet(walletId(command.getSeller()))
                .setOperation(operationId(command.getSaleProcess()))
                .setCurrentBalance(balance)
                .vBuild();
    }

    public static SharesSale sharesSaleInitiatedBy(SellShares command) {
        return SharesSale
                .newBuilder()
                .setId(command.getSaleProcess())
                .setSeller(command.getSeller())
                .setShare(command.getShare())
                .setPrice(command.getPrice())
                .vBuild();
    }

    public static SellSharesOnMarket sellSharesOnMarketWith(SellShares command) {
        return SellSharesOnMarket
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .setSaleProcess(command.getSaleProcess())
                .setShare(command.getShare())
                .setPrice(command.getPrice())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static RechargeBalance rechargeBalanceWith(SellShares command) {
        return RechargeBalance
                .newBuilder()
                .setOperation(operationId(command.getSaleProcess()))
                .setWallet(walletId(command.getSeller()))
                .setMoneyAmount(command.totalCost())
                .vBuild();
    }

    public static SharesSold sharesSoldBy(SellShares command, Investment investment) {
        int sharesAvailable = investment.getSharesAvailable() - command.getQuantity();
        return SharesSold
                .newBuilder()
                .setSaleProcess(command.getSaleProcess())
                .setShare(command.getShare())
                .setSeller(command.getSeller())
                .setPrice(command.getPrice())
                .setSharesAvailable(sharesAvailable)
                .vBuild();
    }

    public static SharesSaleFailed sharesSaleFailedAfter(SellShares command) {
        return SharesSaleFailed
                .newBuilder()
                .setSaleProcess(command.getSaleProcess())
                .setSeller(command.getSeller())
                .vBuild();
    }

    public static InvestmentView investmentViewAfter(SellShares firstSale,
                                                     SellShares secondSale,
                                                     Investment investment) {
        int saleAmount = firstSale.getQuantity() + secondSale.getQuantity();
        int sharesAvailable = investment.getSharesAvailable() - saleAmount;
        return InvestmentView
                .newBuilder()
                .setId(investment.getId())
                .setSharesAvailable(sharesAvailable)
                .vBuild();
    }

    public static WalletBalance walletBalanceAfter(PurchaseShares purchaseCommand,
                                                   SellShares sellCommand,
                                                   Wallet wallet) {
        Money balanceAfterPurchase =
                subtract(wallet.getBalance(), purchaseCommand.totalCost());
        Money currentBalance = sum(balanceAfterPurchase, sellCommand.totalCost());
        return WalletBalance
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(currentBalance)
                .vBuild();
    }

    private static WalletId walletId(UserId owner) {
        return WalletId
                .newBuilder()
                .setOwner(owner)
                .vBuild();
    }

    private static ReplenishmentOperationId operationId(SaleId sale) {
        return ReplenishmentOperationId
                .newBuilder()
                .setSale(sale)
                .vBuild();
    }
}

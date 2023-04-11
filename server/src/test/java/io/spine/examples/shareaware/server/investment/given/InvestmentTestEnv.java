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
import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.WithdrawalOperationId;
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.Investment;
import io.spine.examples.shareaware.investment.SharesPurchase;
import io.spine.examples.shareaware.investment.command.AddShares;
import io.spine.examples.shareaware.investment.command.CancelSharesReservation;
import io.spine.examples.shareaware.investment.command.CompleteSharesReservation;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.command.PurchaseSharesOrBuilder;
import io.spine.examples.shareaware.investment.command.ReserveShares;
import io.spine.examples.shareaware.investment.command.SellShares;
import io.spine.examples.shareaware.investment.event.SharesAdded;
import io.spine.examples.shareaware.investment.event.SharesPurchaseFailed;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.investment.event.SharesReservationCanceled;
import io.spine.examples.shareaware.investment.event.SharesReservationCompleted;
import io.spine.examples.shareaware.investment.event.SharesReserved;
import io.spine.examples.shareaware.investment.rejection.Rejections.InsufficientShares;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.server.market.MarketProcess;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.examples.shareaware.server.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;
import static io.spine.examples.shareaware.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.MoneyCalculator.sum;

public final class InvestmentTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private InvestmentTestEnv() {
    }

    public static PurchaseShares purchaseSharesFor(UserId purchaser) {
        return purchaseSharesFor(purchaser, ShareId.generate());
    }

    public static PurchaseShares purchaseSharesFor(UserId purchaser, ShareId share) {
        return PurchaseShares
                .newBuilder()
                .setShare(share)
                .setPurchaseProcess(PurchaseId.generate())
                .setQuantity(5)
                .setPrice(usd(20))
                .setPurchaser(purchaser)
                .vBuild();
    }

    public static Wallet walletAfter(PurchaseShares firstPurchase,
                                     PurchaseShares secondPurchase,
                                     Wallet wallet) {
        Money commonPurchasePrice = sum(firstPurchase.totalCost(),
                                        secondPurchase.totalCost());
        Money newBalance = subtract(wallet.getBalance(), commonPurchasePrice);
        return wallet
                .toBuilder()
                .setBalance(newBalance)
                .vBuild();
    }

    public static MoneyReserved moneyReservedBy(PurchaseShares command) {
        return MoneyReserved
                .newBuilder()
                .setOperation(operationId(command.getPurchaseProcess()))
                .setWallet(walletId(command.getPurchaser()))
                .setAmount(command.totalCost())
                .vBuild();
    }

    public static ReservedMoneyDebited
    reservedMoneyDebitedBy(PurchaseShares command, Wallet wallet) {
        Money newBalance = subtract(wallet.getBalance(), command.totalCost());
        return ReservedMoneyDebited
                .newBuilder()
                .setWallet(wallet.getId())
                .setOperation(operationId(command.getPurchaseProcess()))
                .setCurrentBalance(newBalance)
                .vBuild();
    }

    public static SharesPurchase sharesPurchaseStateWhen(PurchaseShares command) {
        return SharesPurchase
                .newBuilder()
                .setId(command.getPurchaseProcess())
                .setPurchaser(command.getPurchaser())
                .setShare(command.getShare())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static InsufficientFunds insufficientFundsIn(WalletId wallet, PurchaseShares command) {
        return InsufficientFunds
                .newBuilder()
                .setWallet(wallet)
                .setOperation(operationId(command.getPurchaseProcess()))
                .setAmount(command.totalCost())
                .vBuild();
    }

    public static ReserveMoney reserveMoneyInitiatedBy(PurchaseShares command) {
        return ReserveMoney
                .newBuilder()
                .setWallet(walletId(command.getPurchaser()))
                .setOperation(operationId(command.getPurchaseProcess()))
                .setAmount(command.totalCost())
                .vBuild();
    }

    public static ObtainShares obtainSharesWith(PurchaseShares command) {
        return ObtainShares
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .setPurchase(command.getPurchaseProcess())
                .setShare(command.getShare())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static AddShares addSharesWith(PurchaseShares command) {
        UserId purchaser = command.getPurchaser();
        ShareId share = command.getShare();
        return AddShares
                .newBuilder()
                .setInvestment(investmentId(purchaser, share))
                .setProcess(command.getPurchaseProcess())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static DebitReservedMoney debitReservedMoneyWith(PurchaseShares command) {
        return DebitReservedMoney
                .newBuilder()
                .setOperation(operationId(command.getPurchaseProcess()))
                .setWallet(walletId(command.getPurchaser()))
                .vBuild();
    }

    public static SharesPurchased sharesPurchasedAsResultOf(PurchaseShares command) {
        return SharesPurchased
                .newBuilder()
                .setPurchaseProcess(command.getPurchaseProcess())
                .setPurchaser(command.getPurchaser())
                .setShare(command.getShare())
                .setSharesAvailable(command.getQuantity())
                .vBuild();
    }

    public static SharesPurchaseFailed
    sharesPurchaseFailedAsResultOf(PurchaseShares command) {
        return SharesPurchaseFailed
                .newBuilder()
                .setPurchaseProcess(command.getPurchaseProcess())
                .setPurchaser(command.getPurchaser())
                .vBuild();
    }

    public static Investment investmentAfter(PurchaseShares firstPurchase,
                                             PurchaseShares secondPurchase) {
        UserId purchaser = firstPurchase.getPurchaser();
        ShareId share = firstPurchase.getShare();
        int availableShares = firstPurchase.getQuantity() + secondPurchase.getQuantity();
        return Investment
                .newBuilder()
                .setSharesAvailable(availableShares)
                .setId(investmentId(purchaser, share))
                .vBuild();
    }

    public static MoneyReservationCanceled
    moneyReservationCanceledAfter(PurchaseShares command) {
        return MoneyReservationCanceled
                .newBuilder()
                .setWallet(walletId(command.getPurchaser()))
                .setOperation(operationId(command.getPurchaseProcess()))
                .vBuild();
    }

    public static CancelMoneyReservation
    cancelMoneyReservationAfter(PurchaseSharesOrBuilder command) {
        return CancelMoneyReservation
                .newBuilder()
                .setOperation(operationId(command.getPurchaseProcess()))
                .setWallet(walletId(command.getPurchaser()))
                .vBuild();
    }

    public static SharesAdded sharesAddedBy(PurchaseShares command) {
        UserId purchaser = command.getPurchaser();
        ShareId share = command.getShare();
        return SharesAdded
                .newBuilder()
                .setProcess(command.getPurchaseProcess())
                .setInvestment(investmentId(purchaser, share))
                .setSharesAvailable(command.getQuantity())
                .vBuild();
    }

    public static Investment setUpInvestment(BlackBoxContext context) {
        Wallet wallet = setUpReplenishedWallet(context);
        UserId user = wallet.getId()
                            .getOwner();
        PurchaseShares command = purchaseSharesFor(user);
        context.receivesCommand(command);
        return Investment
                .newBuilder()
                .setId(investmentId(user, command.getShare()))
                .setSharesAvailable(command.getQuantity())
                .vBuild();
    }

    public static SharesReserved sharesReservedBy(SellShares command) {
        return SharesReserved
                .newBuilder()
                .setInvestment(investmentId(command))
                .setProcess(command.getSaleProcess())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static Investment investmentAfter(SellShares command,
                                             Investment investment) {
        int newSharesAvailable = investment.getSharesAvailable() - command.getQuantity();
        return Investment
                .newBuilder()
                .setId(investmentId(command))
                .setSharesAvailable(newSharesAvailable)
                .vBuild();
    }

    public static SharesReservationCompleted
    sharesReservationCompletedBy(SellShares command, Investment investment) {
        int newSharesAvailable = investment.getSharesAvailable() - command.getQuantity();
        return SharesReservationCompleted
                .newBuilder()
                .setProcess(command.getSaleProcess())
                .setInvestment(investment.getId())
                .setSharesAvailable(newSharesAvailable)
                .vBuild();
    }

    public static SharesReservationCanceled
    sharesReservationCanceledAfter(SellShares command) {
        return SharesReservationCanceled
                .newBuilder()
                .setProcess(command.getSaleProcess())
                .setInvestment(investmentId(command))
                .vBuild();
    }

    public static InsufficientShares insufficientSharesCausedBy(SellShares command) {
        return InsufficientShares
                .newBuilder()
                .setInvestment(investmentId(command))
                .setProcess(command.getSaleProcess())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static ReserveShares reserveSharesWith(SellShares command) {
        return ReserveShares
                .newBuilder()
                .setInvestment(investmentId(command))
                .setProcess(command.getSaleProcess())
                .setQuantity(command.getQuantity())
                .vBuild();
    }

    public static CompleteSharesReservation
    completeSharesReservationWith(SellShares command) {
        return CompleteSharesReservation
                .newBuilder()
                .setInvestment(investmentId(command))
                .setProcess(command.getSaleProcess())
                .vBuild();
    }

    public static CancelSharesReservation
    cancelSharesReservationBy(SellShares command) {
        return CancelSharesReservation
                .newBuilder()
                .setProcess(command.getSaleProcess())
                .setInvestment(investmentId(command))
                .vBuild();
    }

    public static InvestmentView investmentViewAfter(PurchaseShares firstPurchase,
                                                     PurchaseShares secondPurchase) {
        UserId user = firstPurchase.getPurchaser();
        ShareId share = firstPurchase.getShare();
        int sharesAvailable = firstPurchase.getQuantity() + secondPurchase.getQuantity();
        return InvestmentView
                .newBuilder()
                .setId(investmentId(user, share))
                .setSharesAvailable(sharesAvailable)
                .vBuild();
    }

    public static WalletBalance walletBalanceAfter(PurchaseShares firstPurchase,
                                                   PurchaseShares secondPurchase,
                                                   Wallet wallet) {
        Money commonPurchasePrice = sum(firstPurchase.totalCost(),
                                        secondPurchase.totalCost());
        Money currentBalance = subtract(wallet.getBalance(), commonPurchasePrice);
        return WalletBalance
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(currentBalance)
                .vBuild();
    }

    private static WithdrawalOperationId operationId(PurchaseId id) {
        return WithdrawalOperationId
                .newBuilder()
                .setPurchase(id)
                .vBuild();
    }

    private static WalletId walletId(UserId owner) {
        return WalletId
                .newBuilder()
                .setOwner(owner)
                .vBuild();
    }

    public static InvestmentId investmentId(UserId owner, ShareId share) {
        return InvestmentId
                .newBuilder()
                .setOwner(owner)
                .setShare(share)
                .vBuild();
    }

    private static InvestmentId investmentId(SellShares command) {
        return investmentId(command.getSeller(), command.getShare());
    }
}

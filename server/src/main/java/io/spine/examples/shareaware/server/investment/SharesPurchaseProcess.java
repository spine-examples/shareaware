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

package io.spine.examples.shareaware.server.investment;

import io.spine.core.UserId;
import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.WithdrawalOperationId;
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.SharesPurchase;
import io.spine.examples.shareaware.investment.command.AddShares;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchaseFailed;
import io.spine.examples.shareaware.investment.event.SharesAdded;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.market.event.SharesObtained;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained;
import io.spine.examples.shareaware.server.market.MarketProcess;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

/**
 * Coordinates the shares purchase from the market.
 */
final class SharesPurchaseProcess
        extends ProcessManager<PurchaseId, SharesPurchase, SharesPurchase.Builder> {

    /**
     * Issues a command to reserve money for shares purchase.
     */
    @Command
    ReserveMoney on(PurchaseShares c) {
        initState(c);
        return ReserveMoney
                .newBuilder()
                .setWallet(walletId(c.getPurchaser()))
                .setOperation(operationId(c.getPurchaseProcess()))
                .setAmount(c.totalCost())
                .vBuild();
    }

    private void initState(PurchaseShares c) {
        builder()
                .setId(c.getPurchaseProcess())
                .setPurchaser(c.getPurchaser())
                .setShare(c.getShare())
                .setQuantity(c.getQuantity());
    }

    /**
     * Terminates the process when there are insufficient funds
     * for shares purchased in the wallet.
     */
    @React
    SharesPurchaseFailed on(InsufficientFunds r) {
        setArchived(true);
        return SharesPurchaseFailed
                .newBuilder()
                .setPurchaseProcess(r.purchaseProcess())
                .setPurchaser(builder().getPurchaser())
                .vBuild();
    }

    /**
     * Issues the command to obtain shares from the market
     * after money for it was reserved.
     */
    @Command
    ObtainShares on(MoneyReserved e) {
        return ObtainShares
                .newBuilder()
                .setPurchase(e.purchaseProcess())
                .setShare(builder().getShare())
                .setQuantity(builder().getQuantity())
                .setMarket(MarketProcess.ID)
                .vBuild();
    }

    /**
     * Issues the command to add shares to the user's investment
     * after they were bought from the market.
     */
    @Command
    AddShares on(SharesObtained e) {
        return AddShares
                .newBuilder()
                .setInvestment(investmentId())
                .setProcess(e.getPurchaseProcess())
                .setQuantity(e.getQuantity())
                .vBuild();
    }

    /**
     * Issues the command to cancel money reservation made for shares purchase
     * after the unexpected error in the shares market.
     */
    @Command
    CancelMoneyReservation on(SharesCannotBeObtained r) {
        return CancelMoneyReservation
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .vBuild();
    }

    /**
     * Terminates the process after the money reservation for shares purchase is canceled.
     */
    @React
    SharesPurchaseFailed on(MoneyReservationCanceled e) {
        setArchived(true);
        return SharesPurchaseFailed
                .newBuilder()
                .setPurchaseProcess(builder().getId())
                .setPurchaser(builder().getPurchaser())
                .vBuild();
    }

    /**
     * Issues the command to debit the reserved money for shares purchase
     * after the shares were bought from the market.
     */
    @Command
    DebitReservedMoney on(SharesAdded e) {
        builder().setSharesAvailable(e.getSharesAvailable());
        return DebitReservedMoney
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .vBuild();
    }

    /**
     * Ends the process successfully when reserved money is debited from the wallet.
     */
    @React
    SharesPurchased on(ReservedMoneyDebited e) {
        setArchived(true);
        return SharesPurchased
                .newBuilder()
                .setPurchaseProcess(builder().getId())
                .setPurchaser(builder().getPurchaser())
                .setShare(builder().getShare())
                .setSharesAvailable(builder().getSharesAvailable())
                .vBuild();
    }

    private InvestmentId investmentId() {
        return InvestmentId
                .newBuilder()
                .setShare(builder().getShare())
                .setOwner(builder().getPurchaser())
                .vBuild();
    }

    private WalletId walletId() {
        return WalletId
                .newBuilder()
                .setOwner(builder().getPurchaser())
                .vBuild();
    }

    private static WalletId walletId(UserId owner) {
        return WalletId
                .newBuilder()
                .setOwner(owner)
                .vBuild();
    }

    private WithdrawalOperationId operationId() {
        return WithdrawalOperationId
                .newBuilder()
                .setPurchase(builder().getId())
                .vBuild();
    }

    private static WithdrawalOperationId operationId(PurchaseId id) {
        return WithdrawalOperationId
                .newBuilder()
                .setPurchase(id)
                .vBuild();
    }
}

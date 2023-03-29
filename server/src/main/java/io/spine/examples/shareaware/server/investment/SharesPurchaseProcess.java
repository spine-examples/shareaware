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

import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.MarketId;
import io.spine.examples.shareaware.OperationId;
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
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.*;

final class SharesPurchaseProcess
        extends ProcessManager<PurchaseId, SharesPurchase, SharesPurchase.Builder> {

    @Command
    ReserveMoney on(PurchaseShares c) {
        initState(c);
        Money purchasePrice =
                multiply(c.getSharePrice(), c.getQuantity());
        return ReserveMoney
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .setAmount(purchasePrice)
                .vBuild();
    }

    private void initState(PurchaseShares c) {
        builder()
                .setId(c.getPurchaseProcess())
                .setPurchaser(c.getPurchaser())
                .setShare(c.getShare())
                .setQuantity(c.getQuantity());
    }

    @React
    SharesPurchaseFailed on(InsufficientFunds r) {
        setArchived(true);
        return SharesPurchaseFailed
                .newBuilder()
                .setPurchaseProcess(r.purchaseProcess())
                .setPurchaser(state().getPurchaser())
                .vBuild();
    }

    @Command
    ObtainShares on(MoneyReserved e) {
        return ObtainShares
                .newBuilder()
                .setPurchase(e.purchaseProcess())
                .setShare(state().getShare())
                .setQuantity(state().getQuantity())
                .setMarket(MarketId.generate()) //Change to static ID
                .vBuild();
    }

    @Command
    AddShares on(SharesObtained e) {
        return AddShares
                .newBuilder()
                .setInvestment(investmentId())
                .setProcess(e.getPurchaseProcess())
                .setQuantity(e.getQuantity())
                .vBuild();
    }

    @React
    CancelMoneyReservation on(SharesCannotBeObtained r) {
        return CancelMoneyReservation
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .vBuild();
    }

    @React
    SharesPurchaseFailed on(MoneyReservationCanceled e) {
        setArchived(true);
        return SharesPurchaseFailed
                .newBuilder()
                .setPurchaseProcess(state().getId())
                .setPurchaser(state().getPurchaser())
                .vBuild();
    }

    @Command
    DebitReservedMoney on(SharesAdded e) {
        return DebitReservedMoney
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .vBuild();
    }

    @React
    SharesPurchased on(ReservedMoneyDebited e) {
        setArchived(true);
        return SharesPurchased
                .newBuilder()
                .setPurchaseProcess(state().getId())
                .setPurchaser(state().getPurchaser())
                .setShare(state().getShare())
                .setQuantity(state().getQuantity())
                .vBuild();
    }

    private InvestmentId investmentId() {
        return InvestmentId
                .newBuilder()
                .setShare(state().getShare())
                .setOwner(state().getPurchaser())
                .vBuild();
    }

    private WalletId walletId() {
        return WalletId
                .newBuilder()
                .setOwner(state().getPurchaser())
                .vBuild();
    }

    private OperationId operationId() {
        return OperationId
                .newBuilder()
                .setPurchase(state().getId())
                .vBuild();
    }
}

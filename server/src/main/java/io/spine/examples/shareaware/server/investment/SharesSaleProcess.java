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
import io.spine.examples.shareaware.ReplenishmentOperationId;
import io.spine.examples.shareaware.SaleId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.SharesSale;
import io.spine.examples.shareaware.investment.command.CancelSharesReservation;
import io.spine.examples.shareaware.investment.command.CompleteSharesReservation;
import io.spine.examples.shareaware.investment.command.ReserveShares;
import io.spine.examples.shareaware.investment.command.SellShares;
import io.spine.examples.shareaware.investment.event.SharesReservationCanceled;
import io.spine.examples.shareaware.investment.event.SharesReservationCompleted;
import io.spine.examples.shareaware.investment.event.SharesReserved;
import io.spine.examples.shareaware.investment.event.SharesSaleFailed;
import io.spine.examples.shareaware.investment.event.SharesSold;
import io.spine.examples.shareaware.investment.rejection.Rejections.InsufficientShares;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.market.event.SharesSoldOnMarket;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeSoldOnMarket;
import io.spine.examples.shareaware.server.market.MarketProcess;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

/**
 * Coordinates the shares sale to the market.
 */
final class SharesSaleProcess
        extends ProcessManager<SaleId, SharesSale, SharesSale.Builder> {

    /**
     * Issues the command to reserve shares for their sale.
     */
    @Command
    ReserveShares on(SellShares c) {
        initState(c);
        var user = c.getSeller();
        var share = c.getShare();
        return ReserveShares
                .newBuilder()
                .setProcess(c.getSaleProcess())
                .setInvestment(investmentId(user, share))
                .setQuantity(c.getQuantity())
                .vBuild();
    }

    private void initState(SellShares c) {
        builder()
                .setId(c.getSaleProcess())
                .setShare(c.getShare())
                .setSeller(c.getSeller())
                .setPrice(c.getPrice());
    }

    /**
     * Issues a command to sell shares on the market
     * after the wanted amount of shares was reserved.
     */
    @Command
    SellSharesOnMarket on(SharesReserved e) {
        return SellSharesOnMarket
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .setSaleProcess(e.getProcess())
                .setShare(builder().getShare())
                .setPrice(builder().getPrice())
                .setQuantity(e.getQuantity())
                .vBuild();
    }

    /**
     * Terminates the process when there is an insufficient number of shares
     * that the user want to sell.
     */
    @React
    SharesSaleFailed on(InsufficientShares r) {
        setArchived(true);
        return SharesSaleFailed
                .newBuilder()
                .setSaleProcess(r.getProcess())
                .setSeller(builder().getSeller())
                .vBuild();
    }

    /**
     * Issues a command to recharge the wallet balance
     * after shares were sold on the market.
     */
    @Command
    RechargeBalance on(SharesSoldOnMarket e) {
        return RechargeBalance
                .newBuilder()
                .setWallet(walletId())
                .setOperation(operationId())
                .setMoneyAmount(e.getPrice())
                .vBuild();
    }

    /**
     * Issues a command to cancel shares reservation made for their sale
     * after the unexpected error in the shares market.
     */
    @Command
    CancelSharesReservation on(SharesCannotBeSoldOnMarket r) {
        return CancelSharesReservation
                .newBuilder()
                .setInvestment(investmentId())
                .setProcess(r.getSaleProcess())
                .vBuild();
    }

    /**
     * Terminates the process after the shares reservation for sale operation is canceled.
     */
    @React
    SharesSaleFailed on(SharesReservationCanceled e) {
        setArchived(true);
        return SharesSaleFailed
                .newBuilder()
                .setSaleProcess(e.getProcess())
                .setSeller(builder().getSeller())
                .vBuild();
    }

    /**
     * Issues a command to complete the shares reservation
     * after the wallet balance is recharged.
     */
    @Command
    CompleteSharesReservation on(BalanceRecharged e) {
        var process = e.getOperation()
                       .getSale();
        return CompleteSharesReservation
                .newBuilder()
                .setInvestment(investmentId())
                .setProcess(process)
                .vBuild();
    }

    /**
     * Ends a process after the shares reservation has been completed.
     */
    @React
    SharesSold on(SharesReservationCompleted e) {
        setArchived(true);
        return SharesSold
                .newBuilder()
                .setSaleProcess(e.getProcess())
                .setSeller(builder().getSeller())
                .setShare(builder().getShare())
                .setPrice(builder().getPrice())
                .setSharesAvailable(e.getSharesAvailable())
                .vBuild();
    }

    private static InvestmentId investmentId(UserId owner, ShareId share) {
        return InvestmentId
                .newBuilder()
                .setOwner(owner)
                .setShare(share)
                .vBuild();
    }

    private InvestmentId investmentId() {
        return InvestmentId
                .newBuilder()
                .setShare(builder().getShare())
                .setOwner(builder().getSeller())
                .vBuild();
    }

    private WalletId walletId() {
        return WalletId
                .newBuilder()
                .setOwner(builder().getSeller())
                .vBuild();
    }

    private ReplenishmentOperationId operationId() {
        return ReplenishmentOperationId
                .newBuilder()
                .setSale(builder().getId())
                .vBuild();
    }
}

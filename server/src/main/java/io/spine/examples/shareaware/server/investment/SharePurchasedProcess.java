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
import io.spine.examples.shareaware.OperationId;
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.SharesPurchase;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.server.wallet.MoneyCalculator;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.money.Money;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;

public class SharePurchasedProcess
        extends ProcessManager<PurchaseId, SharesPurchase, SharesPurchase.Builder> {

    @Command
    ReserveMoney on(PurchaseShares c) {
        initState(c);
        Money purchasePrice =
                MoneyCalculator.multiply(c.getSharePrice(), c.getQuantity());
        return ReserveMoney
                .newBuilder()
                .setWallet(walletId(c.getPurchaser()))
                .setOperation(operationId(c.getPurchaseProcess()))
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

    private static WalletId walletId(UserId owner) {
        return WalletId
                .newBuilder()
                .setOwner(owner)
                .vBuild();
    }

    private static OperationId operationId(PurchaseId purchase) {
        return OperationId
                .newBuilder()
                .setPurchase(purchase)
                .vBuild();
    }
}

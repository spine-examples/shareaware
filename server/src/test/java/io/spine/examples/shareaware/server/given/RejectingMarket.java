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

package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.MarketId;
import io.spine.examples.shareaware.market.Market;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.market.event.SharesObtained;
import io.spine.examples.shareaware.market.rejection.SharesCannotBeObtained;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;

/**
 * The test imitation of {@code MarketProcess} with rejection mode.
 *
 * <p>It has the functionality to enter the rejection mode,
 * which will reject all commands routed to it.
 */
public class RejectingMarket
        extends ProcessManager<MarketId, Market, Market.Builder> {

    private static boolean rejectionMode = false;

    /**
     * Emits the {@code SharesObtained} event when rejection mode if off
     * otherwise throws the {@code SharesCannotBeObtained} rejection.
     */
    @Assign
    SharesObtained on(ObtainShares c) throws SharesCannotBeObtained {
        if (rejectionMode) {
            throw SharesCannotBeObtained
                    .newBuilder()
                    .setPurchaseProcess(c.getPurchase())
                    .build();
        }
        return SharesObtained
                .newBuilder()
                .setMarket(c.getMarket())
                .setPurchaseProcess(c.getPurchase())
                .setShare(c.getShare())
                .setQuantity(c.getQuantity())
                .vBuild();
    }

    public static void switchToRejectionMode() {
        rejectionMode = true;
    }

    public static void switchToEventsMode() {
        rejectionMode = false;
    }
}

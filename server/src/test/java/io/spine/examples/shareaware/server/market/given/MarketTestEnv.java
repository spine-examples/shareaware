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

package io.spine.examples.shareaware.server.market.given;

import io.spine.base.Time;
import io.spine.core.UserId;
import io.spine.examples.shareaware.MarketId;
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.SaleId;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.market.Market;
import io.spine.examples.shareaware.market.command.CloseMarket;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.market.command.OpenMarket;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.market.event.MarketClosed;
import io.spine.examples.shareaware.market.event.MarketOpened;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeSoldOnMarket;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained;
import io.spine.examples.shareaware.given.GivenMoney;
import io.spine.examples.shareaware.server.market.MarketProcess;

import static io.spine.base.Identifier.*;
import static io.spine.examples.shareaware.server.given.GivenShare.*;

public final class MarketTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private MarketTestEnv() {
    }

    public static OpenMarket openMarket() {
        return OpenMarket
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .vBuild();
    }

    public static CloseMarket closeMarket() {
        return CloseMarket
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .vBuild();
    }

    public static Market marketAfter(OpenMarket command) {
        return market(command.getMarket(), false);
    }

    public static Market marketAfter(CloseMarket command) {
        return market(command.getMarket(), true);
    }

    private static Market market(MarketId id, boolean closed) {
        return Market
                .newBuilder()
                .setId(id)
                .setClosed(closed)
                .vBuild();
    }

    public static MarketClosed marketClosedAfter(CloseMarket command) {
        return MarketClosed
                .newBuilder()
                .setMarket(command.getMarket())
                .vBuild();
    }

    public static MarketOpened marketOpenedAfter(OpenMarket command) {
        return MarketOpened
                .newBuilder()
                .setMarket(command.getMarket())
                .vBuild();
    }

    public static ObtainShares obtainShares() {
        return ObtainShares
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .setPurchase(PurchaseId.generate())
                .setShare(ShareId.generate())
                .setQuantity(1)
                .vBuild();
    }

    public static SharesCannotBeObtained
    sharesCannotBeObtainedCausedBy(ObtainShares command) {
        return SharesCannotBeObtained
                .newBuilder()
                .setPurchaseProcess(command.getPurchase())
                .vBuild();
    }

    public static SellSharesOnMarket sellSharesOnMarket() {
        return SellSharesOnMarket
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .setSaleProcess(SaleId.generate())
                .setShare(ShareId.generate())
                .setPrice(GivenMoney.usd(20))
                .setQuantity(1)
                .vBuild();
    }

    public static SharesCannotBeSoldOnMarket
    sharesCannotBeSoldOnMarketCausedBy(SellSharesOnMarket command) {
        return SharesCannotBeSoldOnMarket
                .newBuilder()
                .setSaleProcess(command.getSaleProcess())
                .vBuild();
    }

    public static MarketSharesUpdated marketSharesUpdated() {
        return MarketSharesUpdated
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .addShare(tesla())
                .addShare(apple())
                .setWhenUpdated(Time.currentTime())
                .vBuild();
    }

    public static AvailableMarketShares
    availableMarketSharesAfter(MarketSharesUpdated event) {
        return AvailableMarketShares
                .newBuilder()
                .setId(MarketProcess.ID)
                .addAllShare(event.getShareList())
                .vBuild();
    }

    public static Share shareWithoutPrice(Share share) {
        return Share
                .newBuilder()
                .setId(share.getId())
                .setCompanyName(share.getCompanyName())
                .setCompanyLogo(share.getCompanyLogo())
                .buildPartial();
    }
}

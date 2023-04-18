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

package io.spine.examples.shareaware.server.market;

import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.ShareId;
import io.spine.money.Currency;
import io.spine.money.Money;

/**
 * Provides the instances of the existing shares.
 *
 * @implNote This is a temporary solution for storing shares in the ShareAware application.
 * In the next implementation, all shares data will be stored in the project resources.
 */
final class AvailableShares {

    /**
     * Prevents instantiation of this class.
     */
    private AvailableShares() {
    }

    /**
     * Returns the share of the "Apple" company.
     */
    static Share apple() {
        return Share
                .newBuilder()
                .setId(SharesData.APPLE.id)
                .setPrice(usd(200))
                .setCompanyName("Apple")
                .setCompanyLogo(SharesData.APPLE.logoUrl)
                .vBuild();
    }

    /**
     * Returns the share of the "Tesla" company.
     */
    static Share tesla() {
        return Share
                .newBuilder()
                .setId(SharesData.TESLA.id)
                .setPrice(usd(300))
                .setCompanyName("Tesla Inc.")
                .setCompanyLogo(SharesData.TESLA.logoUrl)
                .vBuild();
    }

    /**
     * Return the share of the "Meta Platforms" company.
     */
    static Share meta() {
        return Share
                .newBuilder()
                .setId(SharesData.META.id)
                .setPrice(usd(150))
                .setCompanyName("Meta Platforms Inc")
                .setCompanyLogo(SharesData.META.logoUrl)
                .vBuild();
    }

    private static Money usd(long units) {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(units)
                .vBuild();
    }

    /**
     * Represents the ID and URL to the company logo for available shares.
     */
    private enum SharesData {
        APPLE(ShareId.generate(),
              "https://upload.wikimedia.org/wikipedia/commons/8/8a/Apple_Logo.svg"),
        TESLA(ShareId.generate(),
              "https://upload.wikimedia.org/wikipedia/commons/b/bd/Tesla_Motors.svg"),
        META(ShareId.generate(),
             "https://upload.wikimedia.org/wikipedia/commons/7/7b/Meta_Platforms_Inc._logo.svg");

        private final ShareId id;
        private final String logoUrl;

        SharesData(ShareId id, String logoUrl) {
            this.id = id;
            this.logoUrl = logoUrl;
        }
    }
}

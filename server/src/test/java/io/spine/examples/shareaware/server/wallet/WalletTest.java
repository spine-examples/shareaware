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

package io.spine.examples.shareaware.server.wallet;

import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.EventSubject;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;

@DisplayName("`Wallet` should")
public final class WalletTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    @Test
    @DisplayName("allow the creation and emit the `WalletCreated` event")
    void event() {
        WalletId wallet = givenId();
        CreateWallet command = createWallet(wallet);
        WalletCreated expectedEvent = WalletCreated
                .newBuilder()
                .setWallet(wallet)
                .setBalance(GivenMoney.zero())
                .vBuild();
        Wallet expectedState = Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(GivenMoney.zero())
                .setReservedMoney(GivenMoney.zero())
                .vBuild();
        context().receivesCommand(command);
        EventSubject assertEvents = context()
                .assertEvents()
                .withType(WalletCreated.class);

        assertEvents.hasSize(1);
        context().assertState(wallet, expectedState);
        context().assertEvent(expectedEvent);
    }
}

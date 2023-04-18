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

package io.spine.examples.shareaware.sharesreader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableSet;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.ShareId;
import io.spine.money.Currency;
import io.spine.money.Money;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

public class SharesReader {

    /**
     * Prevents instantiation of this class.
     */
    private SharesReader() {
    }

    public static Set<Share> read() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        URL urlToFile = requireNonNull(classLoader.getResource("shares.yml"));
        File file = new File(urlToFile.getFile());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JavaType setType = mapper.getTypeFactory()
                                 .constructCollectionType(Set.class, ShareInfo.class);
        try {
            Set<ShareInfo> infos = mapper.readValue(file, setType);
            Set<Share> shares = infos.stream()
                    .map(SharesReader::toShare)
                    .collect(Collectors.toSet());
            return Collections.unmodifiableSet(shares);
        } catch (IOException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    private static Share toShare(ShareInfo data) {
        return Share
                .newBuilder()
                .setId(ShareId.of(data.getId()))
                .setCompanyLogo(data.getCompanyLogo())
                .setCompanyName(data.getCompanyName())
                .setPrice(usd(data.getPriceUnits()))
                .vBuild();
    }

    private static Money usd(int value) {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(value)
                .vBuild();
    }
}

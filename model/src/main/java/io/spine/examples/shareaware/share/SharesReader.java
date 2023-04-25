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

package io.spine.examples.shareaware.share;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.spine.examples.shareaware.ShareId;
import io.spine.money.Currency;
import io.spine.money.Money;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toSet;

/**
 * Provides an API to read {@code Share} instances from the YAML file.
 */
public final class SharesReader {

    /**
     * Prevents instantiation of this class.
     */
    private SharesReader() {
    }

    /**
     * Returns the set (to prevent duplication) of shares read from the provided YAML file.
     *
     * @implNote Shares must be written to the file in this way:
     * <pre> {@code
     *     -
     *     id: value
     *     priceUnits: value
     *     priceNanos: value
     *     companyName: value
     *     companyLogo: value
     *     (next share)
     * }
     * </pre>
     */
    public static Set<Share> read(File file) {
        checkNotNull(file);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        MapLikeType mapType = mapper.getTypeFactory()
                                    .constructMapLikeType(Map.class, String.class, String.class);
        JavaType listType = mapper.getTypeFactory()
                                  .constructCollectionType(List.class, mapType);
        try {
            List<Map<String, String>> sharesData = mapper.readValue(file, listType);
            return sharesData.stream()
                    .map(SharesReader::toShare)
                    .collect(toSet());
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }

    private static Share toShare(Map<String, String> shareData) {
        ShareId id = ShareId.of(shareData.get("id"));
        Money price = priceFrom(shareData.get("priceUnits"), shareData.get("priceNanos"));
        return Share
                .newBuilder()
                .setId(id)
                .setPrice(price)
                .setCompanyName(shareData.get("companyName"))
                .setCompanyLogo(shareData.get("companyLogo"))
                .vBuild();
    }

    private static Money priceFrom(String units, String nanos) {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(parseInt(units))
                .setNanos(parseInt(nanos))
                .vBuild();
    }
}

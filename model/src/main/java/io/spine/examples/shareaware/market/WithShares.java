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

package io.spine.examples.shareaware.market;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.base.EventMessage;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.share.Share;

import java.util.List;

import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * Common interface for signals which operate with a list of shares.
 */
@Immutable
@GeneratedMixin
public interface WithShares extends EventMessage {

    /**
     * Returns the list of shares.
     */
    List<Share> getShareList();

    /**
     * Finds the share with provided ID from the shares list.
     *
     * @throws IllegalArgumentException when the share with provided ID is not found in the list
     */
    default Share find(ShareId id) {
        List<Share> shares = getShareList();
        var optionalShare = shares
                .stream()
                .filter(share -> share.getId().equals(id))
                .findAny();
        if (optionalShare.isEmpty()) {
            throw newIllegalArgumentException(
                    "Cannot find the share with the provided ID `%s` in the list of shares `%s`.",
                    id, shares);
        }
        return optionalShare.get();
    }
}

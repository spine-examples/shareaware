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

import com.google.common.truth.Truth;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static io.spine.examples.shareaware.share.SharesReaderTestEnv.expectedSharesFromFile;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`SharesReader` should")
final class SharesReaderTest extends UtilityClassTest<SharesReader> {

    SharesReaderTest() {
        super(SharesReader.class);
    }

    @Test
    @DisplayName("read shares from file")
    void readShares() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        URL urlToFile = requireNonNull(classLoader.getResource("testing-shares.yml"));
        File file = new File(urlToFile.getFile());
        Set<Share> shares = SharesReader.read(file);
        Set<Share> expected = expectedSharesFromFile();

        Truth.assertThat(shares)
             .isEqualTo(expected);
    }

    @Test
    @DisplayName("throw `IllegalArgumentException` when the provided file is invalid")
    void throwException() {
        File file = new File("notExistingFile.yml");
        assertThrows(IllegalArgumentException.class, () -> SharesReader.read(file));
    }
}
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

import io.spine.examples.shareaware.dependency.ErrorProne
import net.ltgt.gradle.errorprone.errorprone

/**
 * Adds the Error Prone code analyzer to a project and configures it.
 *
 * Also, configures the `Javac` to exclude the generated code from being analyzed
 * and to deal with a few known issues of Error Prone.
 */

plugins {
    java
    id("net.ltgt.errorprone")
}

dependencies {
    errorprone(ErrorProne.CorePlugin.lib)
}

tasks.withType<JavaCompile> {
    with(options) {
        errorprone {
            errorproneArgs.addAll(
                /*
                 * Exclude generated sources from being analyzed by Error Prone.
                 */
                "-XepExcludedPaths:.*/generated/.*",

                /*
                 * Turn the check off until Error Prone can handle `@Nested` JUnit classes.
                 * See issue: https://github.com/google/error-prone/issues/956
                 */
                "-Xep:ClassCanBeStatic:OFF",

                /*
                 * Turn off checks which report unused methods and unused method parameters.
                 * See issue: https://github.com/SpineEventEngine/config/issues/61
                 */
                "-Xep:UnusedMethod:OFF",
                "-Xep:UnusedVariable:OFF",
                "-Xep:CheckReturnValue:OFF"
            )
        }
    }
}

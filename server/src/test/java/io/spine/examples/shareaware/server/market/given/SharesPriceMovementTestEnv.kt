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

package io.spine.examples.shareaware.server.market.given

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import io.spine.base.EntityColumn
import io.spine.base.Time.currentTime
import io.spine.core.ActorContext
import io.spine.core.TenantId
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.SharePriceMovementId
import io.spine.examples.shareaware.market.PriceAtTime
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute
import io.spine.money.Money
import io.spine.testing.core.given.GivenUserId.newUuid

const val ProjectionActivityTime: Long = 60

val ShareFieldInProjection: EntityColumn = SharePriceMovementPerMinute.Column.share()

fun sharePriceMovementPerMinute(
    share: ShareId,
    firstPrice: Money,
    whenFirstPrice: Timestamp,
    secondPrice: Money,
    whenSecondPrice: Timestamp
): SharePriceMovementPerMinute {
    val firstPriceAtTime = PriceAtTime
        .newBuilder()
        .buildWith(firstPrice, whenFirstPrice)
    val secondPriceAtTime = PriceAtTime
        .newBuilder()
        .buildWith(secondPrice, whenSecondPrice)
    val activityTime = Duration
        .newBuilder()
        .setSeconds(ProjectionActivityTime)
        .build()
    val sharePriceMovementId = SharePriceMovementId
        .newBuilder()
        .buildWith(share, activityTime)
    return SharePriceMovementPerMinute
        .newBuilder()
        .setId(sharePriceMovementId)
        .addPriceAtTime(firstPriceAtTime)
        .addPriceAtTime(secondPriceAtTime)
        .setShare(share)
        .buildPartial()
}

fun actorContext(): ActorContext {
    val tenantId = TenantId
        .newBuilder()
        .setValue("SharePriceMovementTest")
        .build()
    return ActorContext
        .newBuilder()
        .setActor(newUuid())
        .setTenantId(tenantId)
        .setTimestamp(currentTime())
        .vBuild()
}

private fun PriceAtTime.Builder.buildWith(
    price: Money,
    time: Timestamp
): PriceAtTime {
    return this
        .setPrice(price)
        .setTime(time)
        .vBuild()
}

private fun SharePriceMovementId.Builder.buildWith(
    share: ShareId,
    activityTime: Duration
): SharePriceMovementId {
    return this
        .setShare(share)
        .setActivityTime(activityTime)
        .buildPartial()
}

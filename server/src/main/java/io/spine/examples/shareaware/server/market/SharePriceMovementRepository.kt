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

package io.spine.examples.shareaware.server.market

import com.google.common.collect.ImmutableSet
import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import io.spine.base.Time.currentTime
import io.spine.client.Filters.eq
import io.spine.core.ActorContext
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.SharePriceMovementId
import io.spine.examples.shareaware.market.SharePriceMovement
import io.spine.examples.shareaware.market.event.MarketSharesUpdated
import io.spine.examples.shareaware.server.ProjectionReader
import io.spine.examples.shareaware.share.Share
import io.spine.server.projection.ProjectionRepository
import io.spine.server.route.EventRouting

public class SharePriceMovementRepository :
    ProjectionRepository<SharePriceMovementId, SharePriceMovementProjection, SharePriceMovement>() {

    override fun setupEventRouting(routing: EventRouting<SharePriceMovementId>) {
        super.setupEventRouting(routing)
        routing.route(MarketSharesUpdated::class.java) { event, context ->
            updateMovements(event, context.actorContext())
        }
    }

    private fun updateMovements(
        event: MarketSharesUpdated, context: ActorContext
    ): ImmutableSet<SharePriceMovementId> {
        val reader = ProjectionReader<SharePriceMovementId, SharePriceMovement>(
            context().stand(),
            SharePriceMovement::class.java
        )
        var activeProjections = setOf<SharePriceMovementId>()
        event.shareList.forEach { share: Share ->
            val shareField = SharePriceMovement.Field.id().share()
            val priceMovements = reader.read(context, eq(shareField, share.id))
            if (priceMovements.isNotEmpty()) {
                var activeProjection = priceMovements.find {
                    it.id.timeRange.greaterThen(currentTime().minus(it.id.whenCreated))
                }?.id
                if (activeProjection == null) {
                    activeProjection = SharePriceMovementId
                        .newBuilder()
                        .buildWith(share.id)
                }
                activeProjections = activeProjections.plus(activeProjection)
            } else {
                val id = SharePriceMovementId
                    .newBuilder()
                    .buildWith(share.id)
                activeProjections = activeProjections.plus(id)
            }
        }
        return ImmutableSet.copyOf(activeProjections)
    }

    private fun SharePriceMovementId.Builder.buildWith(share: ShareId): SharePriceMovementId {
        return this
            .setShare(share)
            .setTimeRange(
                Duration.newBuilder()
                    .setSeconds(60)
                    .build()
            )
            .setWhenCreated(currentTime())
            .vBuild();
    }

    private fun Timestamp.minus(timestamp: Timestamp): Duration {
        val duration = Duration.newBuilder();

        duration.seconds = this.seconds - timestamp.seconds;
        duration.nanos = this.nanos - timestamp.nanos;

        if (duration.seconds < 0 && duration.nanos > 0) {
            duration.seconds += 1;
            duration.nanos -= 1000000000;
        } else if (duration.seconds > 0 && duration.nanos < 0) {
            duration.seconds -= 1;
            duration.nanos += 1000000000;
        }
        val build = duration.build()
        println("Duration $build")
        return build
    }

    private fun Duration.greaterThen(duration: Duration): Boolean {
        if (this.seconds > duration.seconds) {
            return true
        }
        if (this.seconds <= duration.seconds) {
            return false
        }
        return this.nanos > duration.nanos
    }
}

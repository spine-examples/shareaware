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
            routeToSharePriceMovements(event, context.actorContext())
        }
    }

    private fun routeToSharePriceMovements(
        event: MarketSharesUpdated, context: ActorContext
    ): ImmutableSet<SharePriceMovementId> {
        val reader = ProjectionReader<SharePriceMovementId, SharePriceMovement>(
            context().stand(),
            SharePriceMovement::class.java
        )
        var activePriceMovementsIds = setOf<SharePriceMovementId>()
        val shareField = SharePriceMovement.Field.share()
        event.shareList.forEach { share: Share ->
            val sharePriceMovements = reader.read(context, eq(shareField, share.id))
            if (sharePriceMovements.isNotEmpty()) {
                val activePriceMovementId = findActiveOrCreate(sharePriceMovements)
                activePriceMovementsIds = activePriceMovementsIds.plus(activePriceMovementId)
            } else {
                val id = createNewSharePriceMovementId(share.id)
                activePriceMovementsIds = activePriceMovementsIds.plus(id)
            }
        }
        return ImmutableSet.copyOf(activePriceMovementsIds)
    }

    private fun findActiveOrCreate(
        sharePriceMovements: List<SharePriceMovement>
    ): SharePriceMovementId {
        var activeProjectionId = sharePriceMovements.find { priceMovement ->
            val timeFromCreation = currentTime().minus(priceMovement.id.whenCreated)
            priceMovement.id.timeRange.greaterThen(timeFromCreation)
        }?.id
        if (activeProjectionId == null) {
            activeProjectionId = createNewSharePriceMovementId(sharePriceMovements[0].share)
        }
        return activeProjectionId
    }

    private fun createNewSharePriceMovementId(share: ShareId): SharePriceMovementId {
        return SharePriceMovementId
            .newBuilder()
            .buildWith(share)
    }

    private fun SharePriceMovementId.Builder.buildWith(share: ShareId): SharePriceMovementId {
        val duration = Duration
            .newBuilder()
            .setSeconds(60)
            .build()
        return this
            .setShare(share)
            .setTimeRange(duration)
            .setWhenCreated(currentTime())
            .vBuild()
    }
}

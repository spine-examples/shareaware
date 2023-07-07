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

package io.spine.examples.shareaware.server

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import io.spine.base.EntityState
import io.spine.client.ActorRequestFactory
import io.spine.client.EntityStateWithVersion
import io.spine.client.Filter
import io.spine.client.Query
import io.spine.client.QueryResponse
import io.spine.core.ActorContext
import io.spine.grpc.MemoizingObserver
import io.spine.protobuf.AnyPacker
import io.spine.server.stand.Stand

public class ProjectionReader<I, S : EntityState?> (
    private val stand: Stand,
    private val stateClass: Class<S>
) {

    /**
     * Reads projections that match the filter on behalf of the actor from the context.
     */
    public fun read(ctx: ActorContext, vararg filters: Filter): ImmutableList<S> {
        Preconditions.checkNotNull(ctx)
        val queryFactory = ActorRequestFactory
            .fromContext(ctx)
            .query()
        val query = queryFactory
            .select(stateClass)
            .where(*filters)
            .build()
        return executeAndUnpackResponse(query)
    }

    public fun readAll(ctx: ActorContext): ImmutableList<S> {
        Preconditions.checkNotNull(ctx)
        val queryFactory = ActorRequestFactory
            .fromContext(ctx)
            .query()
        val query = queryFactory
            .select(stateClass)
            .build()
        return executeAndUnpackResponse(query)
    }

    private fun executeAndUnpackResponse(query: Query): ImmutableList<S> {
        val observer = MemoizingObserver<QueryResponse>()
        stand.execute(query, observer)
        val response = observer.firstResponse()
        return response
            .messageList
            .stream()
            .map { state: EntityStateWithVersion ->
                AnyPacker.unpack(
                    state.state,
                    stateClass
                )
            }
            .collect(ImmutableList.toImmutableList())
    }
}

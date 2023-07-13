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

package io.spine.examples.shareaware.server;

import com.google.common.collect.ImmutableList;
import io.spine.base.EntityState;
import io.spine.client.ActorRequestFactory;
import io.spine.client.Filter;
import io.spine.client.Query;
import io.spine.client.QueryResponse;
import io.spine.core.ActorContext;
import io.spine.grpc.MemoizingObserver;
import io.spine.server.stand.Stand;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.protobuf.AnyPacker.unpack;

/**
 * Reader for projections in bounded context.
 *
 * @param <S> type of the {@code Projection} to read
 */
public class ProjectionReader<S extends EntityState> {

    private final Stand stand;
    private final Class<S> stateClass;

    public ProjectionReader(Stand stand, Class<S> stateClass) {
        this.stand = checkNotNull(stand);
        this.stateClass = checkNotNull(stateClass);
    }

    /**
     * Reads projections that match the filter on behalf of the actor from the context.
     */
    public ImmutableList<S> read(ActorContext ctx, Filter... filters) {
        checkNotNull(ctx);
        var queryFactory = ActorRequestFactory
                .fromContext(ctx)
                .query();
        var query = queryFactory
                .select(stateClass)
                .where(filters)
                .build();
        return executeAndUnpackResponse(query);
    }

    private ImmutableList<S> executeAndUnpackResponse(Query query) {
        var observer = new MemoizingObserver<QueryResponse>();
        stand.execute(query, observer);
        var response = observer.firstResponse();
        var result = response
                .getMessageList()
                .stream()
                .map(state -> unpack(state.getState(), stateClass))
                .collect(toImmutableList());
        return result;
    }
}

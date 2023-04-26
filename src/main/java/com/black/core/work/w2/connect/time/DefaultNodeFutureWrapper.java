package com.black.core.work.w2.connect.time;

import lombok.AllArgsConstructor;

import java.util.concurrent.ScheduledFuture;

@AllArgsConstructor
public class DefaultNodeFutureWrapper implements ScheduledNodeFutureWrapper{

    ScheduledFuture<?> future;
    String nodeId;
    Long lifeTime;
    Long startTime;

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public void cancel() {
        future.cancel(true);
    }

    @Override
    public ScheduledFuture<?> future() {
        return future;
    }

    @Override
    public Long lifeTime() {
        return lifeTime;
    }

    @Override
    public Long startTime() {
        return startTime;
    }
}

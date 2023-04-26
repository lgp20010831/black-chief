package com.black.core.work.w1.time;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.concurrent.ScheduledFuture;

@Setter @AllArgsConstructor
public class DefaultScheduleFutureWrapper implements ScheduledFutureWrapper {

    ScheduledFuture<?> future;
    Long lifeTime;
    Long startTime;

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

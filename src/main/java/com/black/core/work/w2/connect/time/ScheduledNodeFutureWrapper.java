package com.black.core.work.w2.connect.time;

import com.black.core.work.w1.time.ScheduledFutureWrapper;

public interface ScheduledNodeFutureWrapper extends ScheduledFutureWrapper {

    String getNodeId();

    void cancel();
}

package com.black.core.asyn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.*;

@Getter @Setter @ToString
public class AsynConfiguration {

    private int corePoolSize = 4;

    private int timeCorePoolSize = 1;

    private int maximumPoolSize = corePoolSize * 2;

    private long keepAliveTime = 10;

    private TimeUnit unit = TimeUnit.MILLISECONDS;

    private BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();

    private ThreadFactory threadFactory = new AsynThreadFactory();

    private ThreadFactory timerThreadFactory = new AsynTimerThreadFactory();

    private RejectedExecutionHandler handler = new ACallerRunsPolicy();

    public static class ACallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy{
        @Override
        public String toString() {
            return "A handler for rejected tasks that runs the rejected task directly" +
                    " in the calling thread of the execute method, unless the executor" +
                    " has been shut down, in which case the task is discarded.";
        }
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        maximumPoolSize = corePoolSize * 2;
    }
}

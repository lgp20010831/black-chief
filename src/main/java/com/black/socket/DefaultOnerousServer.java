package com.black.socket;

import com.black.core.util.Assert;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultOnerousServer extends OnerousLoopServer{

    private final int inquirySize;

    private final InquiryClientRunnable[] runnables;

    private final AtomicInteger point = new AtomicInteger(0);

    public DefaultOnerousServer(int inquirySize) {
        Assert.trueThrows(inquirySize <= 0, "inquiry size must > 0");
        this.inquirySize = inquirySize;
        runnables = new InquiryClientRunnable[inquirySize];
        for (int i = 0; i < inquirySize; i++) {
            runnables[i] = new InquiryClientRunnable(log, this);
        }
        for (InquiryClientRunnable runnable : runnables) {
            new Thread(runnable).start();
        }
    }

    @Override
    protected InquiryClientRunnable getInquiryClient() {
        if (point.get() >= inquirySize){
            point.set(0);
        }
        return runnables[point.getAndIncrement()];
    }

}

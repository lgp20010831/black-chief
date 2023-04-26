package com.black.core.cache;

import com.black.core.aop.code.AbstractAopTaskQueueAdapter;

public class GlobalAopDispatcherCache {

    private static AbstractAopTaskQueueAdapter abstractAopTaskQueueAdapter;

    public static void setAbstractAopTaskQueueAdapter(AbstractAopTaskQueueAdapter abstractAopTaskQueueAdapter) {
        GlobalAopDispatcherCache.abstractAopTaskQueueAdapter = abstractAopTaskQueueAdapter;
    }

    public static AbstractAopTaskQueueAdapter getAbstractAopTaskQueueAdapter() {
        return abstractAopTaskQueueAdapter;
    }
}

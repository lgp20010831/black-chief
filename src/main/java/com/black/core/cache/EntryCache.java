package com.black.core.cache;

import com.black.core.entry.EntryExtenderDispatcher;

public class EntryCache {

    private static EntryExtenderDispatcher dispatcher;

    public static void setDispatcher(EntryExtenderDispatcher dispatcher) {
        EntryCache.dispatcher = dispatcher;
    }

    public static EntryExtenderDispatcher getDispatcher() {
        return dispatcher;
    }

}

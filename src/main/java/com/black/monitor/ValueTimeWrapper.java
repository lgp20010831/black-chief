package com.black.monitor;

import lombok.Data;

@SuppressWarnings("all") @Data
public class ValueTimeWrapper<V>{

    private V v;

    private long lastActivityTime;

    public ValueTimeWrapper(V v) {
        this.v = v;
        lastActivityTime = System.currentTimeMillis();
    }



}

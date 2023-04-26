package com.black.pattern;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//计数器
public final class Counter {

    private static final Map<Object, AtomicInteger> countMap = new ConcurrentHashMap<>();

    public static int increasingAndCreate(Object voucher){
        AtomicInteger i = countMap.computeIfAbsent(voucher, vcher -> new AtomicInteger(0));
        return i.incrementAndGet();
    }

    public static boolean achieve(Object voucher, int expect){
        AtomicInteger i = countMap.computeIfAbsent(voucher, vcher -> new AtomicInteger(0));
        return i.get() == expect;
    }

    public static void reset(Object voucher){
        reset(voucher, 0);
    }

    public static void reset(Object voucher, int v){
        AtomicInteger i = countMap.computeIfAbsent(voucher, vcher -> new AtomicInteger(0));
        i.set(v);
    }

    public static void remove(Object voucher){
        countMap.remove(voucher);
    }
}

package com.black.core.aop.servlet.flow;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.black.utils.ServiceUtils.now;

public class RequestTimeCount {

    private final FlowTimeUnit unit;

    private Map<String, AtomicInteger> countMap = new ConcurrentHashMap<>();

    public RequestTimeCount(FlowTimeUnit unit) {
        this.unit = unit;
    }

    public void request(){
        String time = getTime(unit);
        AtomicInteger count = countMap.computeIfAbsent(time, t -> new AtomicInteger(0));
        count.incrementAndGet();
    }

    public Map<String, AtomicInteger> getCountMap() {
        return countMap;
    }

    public int queryCount(String time){
        AtomicInteger integer = countMap.get(time);
        return integer == null ? 0 : integer.get();
    }

    public static String getTime(FlowTimeUnit unit){
        switch (unit){
            case SECONDS:
                return now("yyyy-MM-dd HH:mm:ss");
            case HOURS:
                return now("yyyy-MM-dd HH");
            case MINUTES:
                return now("yyyy-MM-dd HH:mm");
            case DAYS:
                return now("yyyy-MM-dd");
            case MONTHS:
                return now("yyyy-MM");
            case YEARS:
                return now("yyyy");
            default:
                throw new IllegalStateException("ill unit: " + unit);
        }
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (String time : countMap.keySet()) {
            String seq = time + " '" +unit.getDesc() + "' 共访问 [" + countMap.get(time).get() + "] 次";
            joiner.add(seq);
        }
        String string = joiner.toString();
        if (string.length() > 170){
            string = string.substring(0, 170) + "......";
        }
        return string;
    }

    public void clear(){
        countMap.clear();
    }
}

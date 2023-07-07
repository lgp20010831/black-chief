package com.black.database.calcite;


import com.black.utils.LocalObject;
import lombok.extern.log4j.Log4j2;
import org.apache.calcite.linq4j.Enumerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 李桂鹏
 * @create 2023-06-26 11:57
 */
@SuppressWarnings("all") @Log4j2
public class MapMemoryEnumerator implements Enumerator<Object[]> {

    private final List<Map<String, Object>> data;

    private final LocalObject<AtomicInteger> index = new LocalObject<>(() -> new AtomicInteger(-1));

    public MapMemoryEnumerator(List<Map<String, Object>> data) {
        this.data = data;
    }

    @Override
    public Object[] current() {
        Map<String, Object> map = data.get(index.current().get());
        return map.values().toArray();
    }

    @Override
    public boolean moveNext() {
        int i = index.current().get();
        if(i < data.size() - 1){
            index.current().incrementAndGet();
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        index.current().set(0);
    }

    @Override
    public void close() {

    }
}

package com.black.core.mybatis.plus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class QueryFactory {

    private final ArrayQueryFactory queryFactory;
    private final ReentrantLock lock;

    public QueryFactory(){
        this(new ArrayQueryFactory());
    }

    public QueryFactory(ArrayQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
        lock = new ReentrantLock();
    }

    public Collection<Map<String, Object>> openQuery(Object singtonSource){
        if (singtonSource == null){
            return new ArrayList<>();
        }
        return openQuery(Collections.singleton(singtonSource));
    }

    public Collection<Map<String, Object>> openQuery(Collection<?> source){
        return openQuery(source, false);
    }

    public Collection<Map<String, Object>> openQuery(Collection<?> source, boolean toSupper) {
        lock.lock();
        try {
            return queryFactory.openQuery(source, toSupper);
        }finally {
            lock.unlock();
        }
    }

}

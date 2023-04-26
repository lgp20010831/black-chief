package com.black.cache;

import com.black.holder.SpringHodler;
import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpringRedisCacher implements Cacher{

    final RedisTemplate<Object, Object> redisTemplate;

    final ValueOperations<Object, Object> ops;

    public SpringRedisCacher(){
        this(SpringHodler.getBeanFactory());
    }

    public SpringRedisCacher(@NonNull BeanFactory beanFactory){
        this(beanFactory.getBean(RedisTemplate.class));
    }

    public SpringRedisCacher(@NonNull RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        ops = redisTemplate.opsForValue();
    }

    @Override
    public Object getCache(Object key) {
        return ops.get(key);
    }

    @Override
    public boolean exists(Object key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Set<Object> keys() {
        return redisTemplate.keys("*");
    }

    @Override
    public Collection<Object> values() {
        return toMap().values();
    }

    @Override
    public int size() {
        return toMap().size();
    }

    @Override
    public Object rename(Object key, Object newkey) {
        redisTemplate.rename(key, newkey);
        return ops.get(newkey);
    }

    @Override
    public Class<?> type(Object key) {
        DataType dataType = redisTemplate.type(key);
        switch (dataType){
            case NONE:
                return null;
            case SET:
                return Set.class;
            case HASH:
                return Object.class;
            case LIST:
                return List.class;
            case ZSET:
                return Object.class;
            case STREAM:
                return byte.class;
            case STRING:
                return String.class;
            default:
                return String.class;
        }
    }

    @Override
    public Object put(Object key, Object value) {
        ops.set(key, value);
        return value;
    }

    @Override
    public Object putWithExpire(Object key, Object value, TimeUnit unit, long time) {
        ops.set(key, value, time, unit);
        return value;
    }

    /**
     * 为缓存增加/减少过期时间
     *
     * @param key     缓存 key
     * @param unit    时间单位
     * @param time    时间值, 如果该值 <0 则为减少时间
     * @param requird 该值为 true 表示缓存中必须存在目标 key, 否则报错, false 则不会强制判断
     */
    @Override
    public void addExpire(Object key, TimeUnit unit, long time, boolean requird) {
        if (exists(key)) {
            Long expire = redisTemplate.getExpire(key);
            long millis = unit.toSeconds(time);
            Object cache = getCache(key);
            putWithExpire(key, cache, TimeUnit.SECONDS, expire + millis);
        }else {
            if (requird){
                throw new NoSuchElementException("no element of key: " + key);
            }
        }
    }

    @Override
    public Object remove(Object key) {
        return redisTemplate.delete(key);
    }

    @Override
    public boolean clear() {
        redisTemplate.delete(keys());
        return true;
    }

    @Override
    public Map<Object, Object> toMap() {
        Map<Object, Object> map = new HashMap<>();
        Set<Object> keys = keys();
        for (Object key : keys) {
            Object value = ops.get(key);
            map.put(key, value);
        }
        return map;
    }
}

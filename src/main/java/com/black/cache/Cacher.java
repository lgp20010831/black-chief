package com.black.cache;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface Cacher {

    default Object getRequirdCache(Object key){
        Object cache = getCache(key);
        if (cache == null){
            throw new NoSuchElementException("requird cache is not existent");
        }
        return cache;
    }

    //根据 key  获取缓存
    Object getCache(Object key);

    //判断缓存中是否存在
    boolean exists(Object key);

    //返回所有key
    Set<Object> keys();

    //返回缓存的所有对象
    Collection<Object> values();

    //缓存的数量
    int size();

    //重命名
    Object rename(Object key, Object newkey);

    //返回该 key 对应缓存的类型
    Class<?> type(Object key);

    /***
     * 获取缓存并转换成指定的类型,
     * 默认的实现是通过 json 的方式进行转换
     * @param key 缓存 key
     * @param type 期望转换成的类型
     * @param <T> 期望类型
     * @return 期望结果
     */
    default <T> T getCache(Object key, Class<T> type){
        Object cache = getCache(key);
        if (cache != null){
            Class<?> cacheClass = cache.getClass();
            if (type.isAssignableFrom(cacheClass)){
                return (T) cache;
            }
            JSONObject json = JsonUtils.letJson(cache);
            return JsonUtils.toBean(json, type);
        }
        return null;
    }

    default <T> T getRequirdCache(Object key, Class<T> type){
        T cache = getCache(key, type);
        if (cache == null){
            throw new NoSuchElementException("requird cache is not existent");
        }
        return cache;
    }

    Object put(Object key, Object value);

    Object putWithExpire(Object key, Object value, TimeUnit unit, long time);

    default void addExpire(Object key, TimeUnit unit, long time){
        addExpire(key, unit, time, false);
    }

    /**
     * 为缓存增加/减少过期时间
     * @param key 缓存 key
     * @param unit 时间单位
     * @param time 时间值, 如果该值 <0 则为减少时间
     * @param requird 该值为 true 表示缓存中必须存在目标 key, 否则报错, false 则不会强制判断
     */
    void addExpire(Object key, TimeUnit unit, long time, boolean requird);

    //删除缓存
    Object remove(Object key);

    //清空缓存
    boolean clear();

    //转成 map 对象
    Map<Object, Object> toMap();
}

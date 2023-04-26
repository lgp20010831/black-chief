package com.black.core.factory;

import java.util.Collection;

@SuppressWarnings("all")
public interface MutesCacheFactory<K, V> extends CacheFactory<K, Collection<V>> {

    default V getSington(K key, boolean multipleTakeFirst){
        Collection<V> mutes = getMutes(key);
        if (mutes == null || mutes.isEmpty()){
            return null;
        }
        if (mutes.size() == 1){
            for (V mute : mutes) {
                return mute;
            }
        }
        if (!multipleTakeFirst){
            throw new FactoryExcetpion(key + " -- 在工厂中不是单例的存在");
        }
        for (V mute : mutes) {
            return mute;
        }
        return null;
    }

    //获取该 key 持有的所有对象
    Collection<V> getMutes(K key);




}

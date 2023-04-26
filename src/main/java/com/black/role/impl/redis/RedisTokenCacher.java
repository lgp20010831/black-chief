package com.black.role.impl.redis;

import com.alibaba.fastjson.JSONArray;
import com.black.cache.SpringRedisCacher;
import com.black.role.Configuration;
import com.black.role.TokenCacher;
import com.black.core.util.Assert;
import com.black.user.User;
import com.black.utils.ServiceUtils;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisTokenCacher implements TokenCacher {

    private final SpringRedisCacher redisCacher;

    public RedisTokenCacher(){
        redisCacher = new SpringRedisCacher();
    }

    public RedisTokenCacher(RedisTemplate<Object, Object> redisTemplate){
        redisCacher = new SpringRedisCacher(redisTemplate);
    }

    @Override
    public void putCache(Configuration configuration, String noseToken, String userToken, User user) {
        String userId = user.getId();
        Assert.notNull(userId, "unknown userId");
        Object noseTokens = redisCacher.getCache(userId);
        JSONArray array;
        if (noseTokens == null){
            array = new JSONArray();
        }else {
            array = JSONArray.parseArray(noseTokens.toString());
        }
        if (!configuration.isMultiplePlacesLogin()) {
            for (Object nt : array) {
                redisCacher.remove(nt);
            }
        }
        redisCacher.put(noseToken, userToken);
        if (!array.contains(noseToken)){
            array.add(noseToken);
        }
        redisCacher.put(user.getId(), array.toJSONString());
    }

    @Override
    public void discardObsoleteToken(Configuration configuration, User user) {
        if (configuration.isMultiplePlacesLogin()){
            return;
        }

        String userId = user.getId();
        Assert.notNull(userId, "unknown userId");
        Object noseTokens = redisCacher.getCache(userId);
        JSONArray array;
        if (noseTokens == null){
            array = new JSONArray();
        }else {
            array = JSONArray.parseArray(noseTokens.toString());
        }
        for (Object nt : array) {
            redisCacher.remove(nt);
        }
        array.clear();
    }


    @Override
    public boolean exists(Configuration configuration, String noseToken) {
        return redisCacher.exists(noseToken);
    }

    @Override
    public String getUserToken(Configuration configuration, String noseToken) {
        return ServiceUtils.getString(redisCacher.getCache(noseToken));
    }

    @Override
    public String removeToken(Configuration configuration, String noseToken) {
        return ServiceUtils.getString(redisCacher.remove(noseToken));
    }
}

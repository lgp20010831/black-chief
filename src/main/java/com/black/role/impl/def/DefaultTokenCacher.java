package com.black.role.impl.def;

import com.black.role.Configuration;
import com.black.role.TokenCacher;
import com.black.core.util.Assert;
import com.black.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTokenCacher implements TokenCacher {

    //key = noseToken, value = token
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    //key = id, value = noseToken
    private final Map<String, List<String>> accountCache = new ConcurrentHashMap<>();

    @Override
    public void putCache(Configuration configuration, String noseToken, String userToken, User user) {
        String userId = user.getId();
        Assert.notNull(userId, "unknown userId");
        List<String> noseTokens = accountCache.computeIfAbsent(userId, id -> new ArrayList<>());
        if (!configuration.isMultiplePlacesLogin()) {
            for (String nt : noseTokens) {
                removeToken(configuration, nt);
            }
        }
        cache.put(noseToken, userToken);
        if (!noseTokens.contains(noseToken)){
            noseTokens.add(noseToken);
        }

    }

    @Override
    public void discardObsoleteToken(Configuration configuration, User user) {
        if (configuration.isMultiplePlacesLogin()) {
            return;
        }
        String userId = user.getId();
        Assert.notNull(userId, "unknown userId");
        List<String> noseTokens = accountCache.computeIfAbsent(userId, id -> new ArrayList<>());
        for (String noseToken : noseTokens) {
            removeToken(configuration, noseToken);
        }
        noseTokens.clear();
    }

    @Override
    public boolean exists(Configuration configuration, String noseToken) {
        return cache.containsKey(noseToken);
    }

    @Override
    public String getUserToken(Configuration configuration, String noseToken) {
        return cache.get(noseToken);
    }

    @Override
    public String removeToken(Configuration configuration, String noseToken) {
        return cache.remove(noseToken);
    }
}

package com.black.role;

import com.black.user.User;

public interface TokenCacher {

    void putCache(Configuration configuration, String noseToken, String userToken, User user);

    void discardObsoleteToken(Configuration configuration, User user);

    boolean exists(Configuration configuration, String noseToken);

    String getUserToken(Configuration configuration, String noseToken);

    String removeToken(Configuration configuration, String noseToken);
}

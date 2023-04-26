package com.black.role;

import com.black.user.User;

public class Tokens {


    public static String login(User user){
        Configuration configuration = ConfigurationHolder.getConfiguration();
        if (configuration != null){
            TokenCacher cacher = configuration.getCacher();
            TokenCreator creator = configuration.getCreator();
            String token = creator.createUserToken(configuration, user);
            String noseToken = creator.createNoseToken(configuration, user);
            cacher.discardObsoleteToken(configuration, user);
            cacher.putCache(configuration, noseToken, token, user);
            return noseToken;
        }
        return null;
    }


}

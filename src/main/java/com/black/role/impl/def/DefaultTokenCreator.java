package com.black.role.impl.def;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.black.role.Configuration;
import com.black.role.TokenCreator;
import com.black.role.TokenUtils;
import com.black.core.json.JsonUtils;
import com.black.core.sql.code.log.Log;
import com.black.user.User;

import java.util.Date;

@SuppressWarnings("all")
public class DefaultTokenCreator implements TokenCreator {

    @Override
    public String createUserToken(Configuration configuration, User user) {
        Date time = TokenUtils.createDate(configuration.getTokenUnit(), configuration.getTokenInvaild());
        String token = TokenUtils.createToken(JsonUtils.letJson(user), time, configuration.getSecretKey());
        Log log = configuration.getLog();
        if (log.isInfoEnabled()) {
            log.info("[token creator] ==> create user token: " + token);
        }
        return token;
    }

    @Override
    public String createNoseToken(Configuration configuration, User user) {
        String noseToken;
        if (configuration.isMultiplePlacesLogin()){
            noseToken = JWT.create().withClaim("id", user.getId()).sign(Algorithm.HMAC256(configuration.getSecretKey()));
        }else {
            noseToken = JWT.create().withExpiresAt(new Date()).sign(Algorithm.HMAC256(configuration.getSecretKey()));
        }
        Log log = configuration.getLog();
        if (log.isInfoEnabled()) {
            log.info("[token creator] ==> create nose token: " + noseToken);
        }
        return noseToken;
    }
}

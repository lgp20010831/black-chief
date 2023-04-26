package com.black.role.impl.def;

import com.alibaba.fastjson.JSONObject;
import com.black.role.*;
import com.black.core.json.JsonUtils;
import com.black.core.servlet.TokenExpirationException;
import com.black.core.sql.code.log.Log;
import com.black.core.util.Assert;
import com.black.user.User;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

public class DefaultTokenResolver implements TokenResolver {

    public final String TOKEN_REQUEST_HEADER = "authorization";;

    public final String TOKEN_PREFIX = "Bearer ";

    final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public String getToken(HttpServletRequest request, Configuration configuration) {
        String header = request.getHeader(TOKEN_REQUEST_HEADER);
        if (header == null || !header.contains(TOKEN_PREFIX))
            return null;
        return header.substring(header.indexOf(TOKEN_PREFIX) + TOKEN_PREFIX.length());
    }

    @Override
    public User verification(Configuration configuration, String token, HttpServletRequest request) throws TokenExpirationException, TokenInvalidException {
        TokenCacher cacher = configuration.getCacher();
        if (!cacher.exists(configuration, token)) {
            throw new TokenInvalidException();
        }
        String userToken = cacher.getUserToken(configuration, token);
        if (userToken == null){
            throw new TokenInvalidException();
        }
        Map<String, Object> map = TokenUtils.parseToken(userToken);
        Class<? extends User> userEntity = getEntity(configuration, request);
        User user = JsonUtils.toBean(new JSONObject(map), userEntity);
        Date expireAt = TokenUtils.getExpireAt(userToken);
        if (expireAt != null){
            if (TokenUtils.isExpire(expireAt)){
                throw new TokenExpirationException();
            }
            prolongateToken(configuration, userToken, token, user);
        }
        return user;
    }

    private Class<? extends User> getEntity(Configuration configuration, HttpServletRequest request){
        Log log = configuration.getLog();
        Map<String, Class<? extends User>> domainWithEntity = configuration.getDomainWithEntity();
        String servletPath = request.getServletPath();
        for (String pattern : domainWithEntity.keySet()) {
            if (pathMatcher.match(pattern, servletPath)) {
                Class<? extends User> e = domainWithEntity.get(pattern);
                if (log.isDebugEnabled()) {
                    log.debug("[token entity] ==> cut token entity: [" + e.getSimpleName() + "]");
                }
                return e;
            }
        }
        Assert.notNull(configuration.getDefaultEntityType(), "unknown current token entity");
        return configuration.getDefaultEntityType();
    }

    private void prolongateToken(Configuration configuration, String userToken, String token, User user){
        Log log = configuration.getLog();
        Map<String, Object> map = TokenUtils.parseToken(userToken);
        Date time = TokenUtils.createDate(configuration.getTokenUnit(), configuration.getTokenInvaild());
        String newestToken = TokenUtils.createToken(map, time, configuration.getSecretKey());
        if (log.isInfoEnabled()) {
            log.info("[token resolver] ==> prolongate token expire time, newest token:" + newestToken);
        }
        TokenCacher cacher = configuration.getCacher();
        cacher.putCache(configuration, token, newestToken, user);

    }
}

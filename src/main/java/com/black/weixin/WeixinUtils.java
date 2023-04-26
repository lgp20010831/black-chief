package com.black.weixin;

import com.alibaba.fastjson.JSONObject;
import com.black.holder.SpringHodler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//提供了缓存 weixin token的功能
@Log4j2
public class WeixinUtils {

    private static WeixinFeign feign;

    private static final Map<String, TokenWrapper> tokenCache = new ConcurrentHashMap<>();

    @Getter @Setter @ToString
    private static class TokenWrapper{
        //凭证
        private String token;
        //过期毫秒值
        private long expiresTime;
    }

    public static void setFeign(WeixinFeign feign) {
        WeixinUtils.feign = feign;
    }

    public static WeixinFeign getFeign() {
        return feign;
    }

    public static Map<String, TokenWrapper> getTokenCache() {
        return tokenCache;
    }

    private static void check(){
        if (feign == null){
            BeanFactory factory = SpringHodler.getBeanFactory();
            if (factory == null){
                throw new IllegalStateException("can not find spring bean factory");
            }
            feign = factory.getBean(WeixinFeign.class);
        }
    }

    public static String getToken(String corpid, String corpsecret){
        check();
        WeixinFeign feign = getFeign();
        TokenWrapper wrapper = tokenCache.computeIfAbsent(corpid, id -> {
            return fetchToken(id, corpsecret);
        });
        if (System.currentTimeMillis() >= wrapper.getExpiresTime() - 5){
            tokenCache.remove(corpid);
            return getToken(corpid, corpsecret);
        }
        log.info("getToken by cache");
        return wrapper.getToken();
    }

    public static TokenWrapper fetchToken(String corpid, String corpsecret){
        check();
        WeixinFeign feign = getFeign();
        JSONObject response = feign.getAccessToken(corpid, corpsecret);
        log.info("fetch token response:{}", response);
        Integer errcode = response.getInteger("errcode");
        if (0 != errcode){
            throw new IllegalStateException("fetch error: " + response.getString("errmsg"));
        }
        TokenWrapper wrapper = new TokenWrapper();
        wrapper.setToken(response.getString("access_token"));
        wrapper.setExpiresTime(System.currentTimeMillis() + (response.getLongValue("expires_in") * 1000));
        log.info("fetch token: {}", wrapper);
        return wrapper;
    }

}

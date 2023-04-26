package com.black.role;

import com.black.core.spring.ChooseScanRangeHolder;
import com.black.core.sql.code.log.Log;
import com.black.core.token.ResponseTokenWriter;
import com.black.core.util.Assert;
import com.black.user.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

@Getter @Setter
public class Configuration {

    private TokenCacher cacher;

    private TokenCreator creator;

    private Log log;

    private boolean globalRange = true;

    //过滤调静态资源的请求
    private boolean filterResourceRequest = true;

    private final Set<String> validatorRange = new HashSet<>();

    private final Set<String> matechFilterPath = new HashSet<>();

    private final Set<String> completeMatchPath = new HashSet<>();

    private final Set<String> completeResourcePath = new HashSet<>();

    private final Set<String> matchResourceFilterPath = new HashSet<>();

    private final LinkedBlockingQueue<TokenListener> listeners = new LinkedBlockingQueue<>();

    private TokenResolver resolver;

    private int tokenUnit = Calendar.MINUTE;

    private int tokenInvaild = 30;

    private String secretKey = "secret_key";

    private Class<? extends User> entity;

    private Class<? extends User> defaultEntityType;

    //是否允许多个地方同时登录一个用户
    private boolean multiplePlacesLogin = false;

    private Function<Throwable, Boolean> throwableCallback;

    //指定特殊的域使用特殊的 user entity
    private final Map<String, Class<? extends User>> domainWithEntity = new ConcurrentHashMap<>();

    //如果需要用多个 user 作为 token, 则这里需要进行配置
    public Configuration putDomain(String pattern, Class<? extends User> entity){
        if (pattern != null && entity != null){
            domainWithEntity.put(pattern, entity);
        }
        return this;
    }

    public Log getLog() {
        Assert.notNull(log, "log is null");
        return log;
    }

    public TokenCreator getCreator() {
        Assert.notNull(creator, "creator is null");
        return creator;
    }

    public TokenCacher getCacher() {
        Assert.notNull(cacher, "cacher is null");
        return cacher;
    }

    public TokenResolver getResolver() {
        Assert.notNull(resolver, "resolver is null");
        return resolver;
    }

    public Configuration registerListener(TokenListener listener){
        if (listener != null){
            listeners.add(listener);
        }
        return this;
    }

    public Configuration registerVaildRange(String... range){
        ChooseScanRangeHolder.filterVaildRange(validatorRange, range);
        return this;
    }

    public Configuration addMatch(String filterPath){
        if (filterPath != null){
            matechFilterPath.add(filterPath);
        }
        return this;
    }

    public Configuration addCompleteMatch(String path){
        if (path != null){
            completeMatchPath.add(path);
        }
        return this;
    }

    public Configuration addCompleteResourceMatch(String path){
        if (path != null){
            completeResourcePath.add(path);
        }
        return this;
    }

    public Configuration addResourceMatch(String filterPath){
        if (filterPath != null){
            matchResourceFilterPath.add(filterPath);
        }
        return this;
    }

    public LinkedBlockingQueue<TokenListener> getListeners() {
        if (listeners.isEmpty()){
            listeners.add(new ResponseTokenWriter());
        }
        return listeners;
    }
}

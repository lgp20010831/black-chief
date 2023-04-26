package com.black.core.netty.code;

import com.black.netty.Configuration;
import com.black.netty.NettySession;
import com.black.netty.Session;
import com.black.netty.SessionFactory;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class NettyUserWrapper {
    public static final String WRAPPER_ALIAS = "_netty_wrapper";
    private final String alias;
    private final SessionFactory<?> sessionFactory;
    private final NettySession nettySession;
    private final Object user;
    private final Map<Object, Object> attributeMap = new HashMap<>();

    public NettyUserWrapper(String alias, SessionFactory<?> sessionFactory, NettySession nettySession, Object user) {
        this.alias = alias;
        this.sessionFactory = sessionFactory;
        this.nettySession = nettySession;
        this.user = user;
        attributeMap.put("alias", alias);
        attributeMap.put(alias + WRAPPER_ALIAS, this);
        attributeMap.put(Configuration.class, sessionFactory.getConfiguration());
        attributeMap.put(SessionFactory.class, sessionFactory);
        attributeMap.put(NettySession.class, nettySession);
        attributeMap.put(Session.class, nettySession);
    }

    public Object get(Object key){
        return attributeMap.get(key);
    }

    public void put(Object key, Object value){
        attributeMap.put(key, value);
    }
}

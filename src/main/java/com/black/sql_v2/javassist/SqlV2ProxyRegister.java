package com.black.sql_v2.javassist;

import com.black.pattern.MethodInvoker;
import com.black.core.chain.GroupKeys;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SqlV2ProxyRegister {

    private static SqlV2ProxyRegister register;

    public synchronized static SqlV2ProxyRegister getInstance() {
        if (register == null){
            register = new SqlV2ProxyRegister();
        }
        return register;
    }

    //alias, tableName, methodName
    private final Map<GroupKeys, MethodInvoker> cache = new ConcurrentHashMap<>();

    public MethodInvoker getMethodInvoker(@NonNull GroupKeys groupKeys){
        return cache.get(groupKeys);
    }

    public void register(GroupKeys groupKeys, MethodInvoker invoker){
        cache.put(groupKeys, invoker);
    }

}

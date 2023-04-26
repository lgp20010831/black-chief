package com.black.mq_v2.proxy.aop;

import com.black.mq_v2.MqttUtils;
import com.black.mq_v2.definition.MqttContext;
import com.black.mq_v2.proxy.RegisterHolder;
import com.black.core.log.IoLog;
import com.black.core.util.StreamUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MqttContextRegister {

    private static MqttContextRegister register;

    public synchronized static MqttContextRegister getInstance() {
        if(register == null){
            register = new MqttContextRegister();
        }
        return register;
    }

    private final Map<String, RegisterHolder> contextCache = new ConcurrentHashMap<>();

    public boolean isSingle(){
        return contextCache.size() == 1;
    }

    public void registerContext(MqttContext context){
        contextCache.put(context.getName(), new RegisterHolder(context));
    }

    public Map<String, RegisterHolder> getContextCache() {
        return contextCache;
    }

    public Collection<RegisterHolder> getContext(String name){
        if (isSingle()){
            return contextCache.values();
        }
        Set<String> nameSet = contextCache.keySet();
        List<String> names = StreamUtils.filterList(nameSet, alias -> MqttUtils.matchPattern(alias, name));
        return StreamUtils.mapList(names, contextCache::get);
    }

    public void clear(){
        contextCache.clear();
    }

    public RegisterHolder remove(String name){
        return contextCache.remove(name);
    }

    public void start(){
        for (RegisterHolder holder : contextCache.values()) {
            MqttContext context = holder.getContext();
            IoLog log = context.getLog();
            try {
                context.connect();
                log.info("successfully started the mq service: " + context.getName());
            }catch (Throwable e){
                log.info("failed to start the mq service: " + context.getName());
            }

        }
    }
}

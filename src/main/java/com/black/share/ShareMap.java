package com.black.share;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:38
 */
@SuppressWarnings("all")
public class ShareMap extends RemoteDecisionMaker{


    public void put(String key, String val){
        invokeMethodCastThrowable("put", key, val);
    }

    public String get(String key){
        return (String) invokeMethodCastThrowable("get", key);
    }

    public String remove(String key){
        return (String) invokeMethodCastThrowable("remove", key);
    }

    public void clear(){
        invokeMethodCastThrowable("clear");
    }

    public Collection<String> keySet(){
        return (Collection<String>) invokeMethodCastThrowable("keySet");
    }

    @Override
    protected ShareServer openServer(String host, int port) {
        return new ShareMapServer(host, port);
    }

    @Override
    protected ShareClient openClient(String host, int port) {
        return new ShareMapClient(host, port);
    }

    static class ShareMapServer extends ReflectionMappingShareServer{

        protected final Map<String, String> map = new LinkedHashMap<>();

        protected ShareMapServer(String host, int port) {
            super(host, port);
        }

        @Override
        public void reset(Object process) {
            if (process != null){
                map.putAll((Map<? extends String, ? extends String>) process);
            }
        }

        @Override
        protected Object getProcess() {
            return map;
        }
    }

    class ShareMapClient extends ShareClient{

        protected ShareMapClient(String host, int port) {
            super(host, port, ShareMap.this);
        }
    }
}

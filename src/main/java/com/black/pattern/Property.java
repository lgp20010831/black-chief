package com.black.pattern;

import com.black.utils.ServiceUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-07-12 15:37
 */
@SuppressWarnings("all") @Data
public class Property {

    private Class<?> type;

    private String name;

    private Class<?> hostType;

    private Object host;

    private boolean isMapHost;

    public Property(String name, Class<?> hostType) {
        this.name = name;
        this.hostType = hostType;
        type = Object.class;
        isMapHost = Map.class.isAssignableFrom(hostType);
    }

    public void set(Object value){
        set(value, host);
    }

    public void set(Object value, Object host){
        if (host == null) return;
        ServiceUtils.setProperty(host, name, value);
    }

    public Object get(){
        return get(host);
    }

    public Object get(Object host){
        if (host == null) return null;
        return ServiceUtils.getProperty(host, name);
    }
}

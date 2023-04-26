package com.black.model;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

public interface Model extends Serializable {

    //转成 map
    Map<String, Object> toMap();

    //序列化
    byte[] getBytes();

    InputStream getInputStream();

    //获取 value
    Object get(String name);
}

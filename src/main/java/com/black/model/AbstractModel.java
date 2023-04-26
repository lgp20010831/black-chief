package com.black.model;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Map;

public abstract class AbstractModel implements Model{

    private final Map<String, Object> map;

    protected AbstractModel(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Map<String, Object> toMap() {
        return map;
    }

    @Override
    public byte[] getBytes() {
        return map.toString().replace('=', ':').getBytes();
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }
}

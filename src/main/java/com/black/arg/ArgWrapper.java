package com.black.arg;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.utils.TypeUtils;

import java.sql.Timestamp;
import java.util.Date;


@SuppressWarnings("ALL")
public class ArgWrapper {

    private final Object[] args;

    public ArgWrapper(Object... args) {
        this.args = args == null ? new Object[0] : args;
    }

    public Object[] getArgs() {
        return args;
    }

    private void check(int index){
        if (index < 0 || index >= args.length){
            throw new IllegalStateException("index out bound: " + index);
        }
    }

    public Object getValue(int index){
        return getValue(index, true);
    }

    public Object getValue(int index, boolean nullable){
        check(index);
        Object arg = args[index];
        if (!nullable && arg == null){
            throw new IllegalStateException("index of " + index + " arg is null");
        }
        return arg;
    }

    public String getString(int index){
        return getString(index, true);
    }

    public String getString(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToString(value);
    }

    public Integer getInteger(int index){
        return getInteger(index, true);
    }

    public Integer getInteger(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToInt(value);
    }

    public Long getLong(int index){
        return getLong(index, true);
    }

    public Long getLong(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToLong(value);
    }

    public Double getDouble(int index){
        return getDouble(index, true);
    }

    public Double getDouble(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToDouble(value);
    }

    public Short getShort(int index){
        return getShort(index, true);
    }

    public Short getShort(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToShort(value);
    }

    public Character getChar(int index){
        return getChar(index, true);
    }

    public Character getChar(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToChar(value);
    }

    public Boolean getBoolean(int index){
        return getBoolean(index, true);
    }

    public Boolean getBoolean(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToBoolean(value);
    }

    public Byte getByte(int index){
        return getByte(index, true);
    }

    public Byte getByte(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToByte(value);
    }

    public Float getFloat(int index){
        return getFloat(index, true);
    }

    public Float getFloat(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToFloat(value);
    }

    public Date getDate(int index){
        return getDate(index, true);
    }

    public Date getDate(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToDate(value);
    }

    public Timestamp getTimestamp(int index){
        return getTimestamp(index, true);
    }

    public Timestamp getTimestamp(int index, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToTimestamp(value);
    }

    public <T> T getObject(int index, Class<T> type){
        return getObject(index, type, true);
    }

    public <T> T getObject(int index, Class<T> type, boolean nullable){
        Object value = getValue(index, nullable);
        return TypeUtils.castToJavaBean(value, type);
    }

    public JSONObject getJson(int index){
        return getJson(index, true);
    }

    public JSONObject getJson(int index, boolean nullable){
        return JsonUtils.letJson(getValue(index, nullable));
    }

    public JSONArray getArray(int index){
        return getArray(index, true);
    }

    public JSONArray getArray(int index, boolean nullable){
        return (JSONArray) JSON.toJSON(getValue(index, nullable));
    }
}

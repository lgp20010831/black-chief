package com.black.standard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public interface AttributeAdaptation {

    Object getByExpression(String expression);

    Object get(Object key);

    default JSONObject getJSONObject(String key) {
        Object value = get(key);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        } else if (value instanceof Map) {
            return new JSONObject((Map)value);
        } else {
            return value instanceof String ? JSON.parseObject((String)value) : (JSONObject)JSON.toJSON(value);
        }
    }

    default JSONArray getJSONArray(String key) {
        Object value = get(key);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        } else if (value instanceof List) {
            return new JSONArray((List)value);
        } else {
            return value instanceof String ? (JSONArray)JSON.parse((String)value) : (JSONArray)JSON.toJSON(value);
        }
    }

    default Boolean getBoolean(String key) {
        Object value = this.get(key);
        return value == null ? null : TypeUtils.castToBoolean(value);
    }

    default byte[] getBytes(String key) {
        Object value = this.get(key);
        return value == null ? null : TypeUtils.castToBytes(value);
    }

    default boolean getBooleanValue(String key) {
        Object value = this.get(key);
        Boolean booleanVal = TypeUtils.castToBoolean(value);
        return booleanVal == null ? false : booleanVal;
    }

    default Byte getByte(String key) {
        Object value = this.get(key);
        return TypeUtils.castToByte(value);
    }

    default byte getByteValue(String key) {
        Object value = this.get(key);
        Byte byteVal = TypeUtils.castToByte(value);
        return byteVal == null ? 0 : byteVal;
    }

    default Short getShort(String key) {
        Object value = this.get(key);
        return TypeUtils.castToShort(value);
    }

    default short getShortValue(String key) {
        Object value = this.get(key);
        Short shortVal = TypeUtils.castToShort(value);
        return shortVal == null ? 0 : shortVal;
    }

    default Integer getInteger(String key) {
        Object value = this.get(key);
        return TypeUtils.castToInt(value);
    }

    default int getIntValue(String key) {
        Object value = this.get(key);
        if ("true".equalsIgnoreCase(String.valueOf(value))){
            return 1;
        }else if ("false".equalsIgnoreCase(String.valueOf(value))){
            return 2;
        }
        Integer intVal = TypeUtils.castToInt(value);
        return intVal == null ? 0 : intVal;
    }

    default Long getLong(String key) {
        Object value = this.get(key);
        return TypeUtils.castToLong(value);
    }

    default long getLongValue(String key) {
        Object value = this.get(key);
        Long longVal = TypeUtils.castToLong(value);
        return longVal == null ? 0L : longVal;
    }

    default Float getFloat(String key) {
        Object value = this.get(key);
        return TypeUtils.castToFloat(value);
    }

    default float getFloatValue(String key) {
        Object value = this.get(key);
        Float floatValue = TypeUtils.castToFloat(value);
        return floatValue == null ? 0.0F : floatValue;
    }

    default Double getDouble(String key) {
        Object value = this.get(key);
        return TypeUtils.castToDouble(value);
    }

    default double getDoubleValue(String key) {
        Object value = this.get(key);
        Double doubleValue = TypeUtils.castToDouble(value);
        return doubleValue == null ? 0.0D : doubleValue;
    }

    default BigDecimal getBigDecimal(String key) {
        Object value = this.get(key);
        return TypeUtils.castToBigDecimal(value);
    }

    default BigInteger getBigInteger(String key) {
        Object value = this.get(key);
        return TypeUtils.castToBigInteger(value);
    }

    default String getString(String key) {
        Object value = this.get(key);
        return value == null ? null : value.toString();
    }

    default Date getDate(String key) {
        Object value = this.get(key);
        return TypeUtils.castToDate(value);
    }

    default JSONObject getJSONObjectByExpression(String key) {
        Object value = getByExpression(key);
        if (value instanceof JSONObject) {
            return (JSONObject)value;
        } else if (value instanceof Map) {
            return new JSONObject((Map)value);
        } else {
            return value instanceof String ? JSON.parseObject((String)value) : (JSONObject)JSON.toJSON(value);
        }
    }

    default JSONArray getJSONArrayByExpression(String key) {
        Object value = getByExpression(key);
        if (value instanceof JSONArray) {
            return (JSONArray)value;
        } else if (value instanceof List) {
            return new JSONArray((List)value);
        } else {
            return value instanceof String ? (JSONArray)JSON.parse((String)value) : (JSONArray)JSON.toJSON(value);
        }
    }


    default Boolean getBooleanByExpression(String key) {
        Object value = this.getByExpression(key);
        return value == null ? null : TypeUtils.castToBoolean(value);
    }

    default byte[] getBytesByExpression(String key) {
        Object value = this.getByExpression(key);
        return value == null ? null : TypeUtils.castToBytes(value);
    }

    default boolean getBooleanValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Boolean booleanVal = TypeUtils.castToBoolean(value);
        return booleanVal == null ? false : booleanVal;
    }

    default Byte getByteByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToByte(value);
    }

    default byte getByteValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Byte byteVal = TypeUtils.castToByte(value);
        return byteVal == null ? 0 : byteVal;
    }

    default Short getShortByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToShort(value);
    }

    default short getShortValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Short shortVal = TypeUtils.castToShort(value);
        return shortVal == null ? 0 : shortVal;
    }

    default Integer getIntegerByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToInt(value);
    }

    default int getIntValueByExpression(String key) {
        Object value = this.getByExpression(key);
        if ("true".equalsIgnoreCase(String.valueOf(value))){
            return 1;
        }else if ("false".equalsIgnoreCase(String.valueOf(value))){
            return 2;
        }
        Integer intVal = TypeUtils.castToInt(value);
        return intVal == null ? 0 : intVal;
    }

    default Long getLongByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToLong(value);
    }

    default long getLongValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Long longVal = TypeUtils.castToLong(value);
        return longVal == null ? 0L : longVal;
    }

    default Float getFloatByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToFloat(value);
    }

    default float getFloatValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Float floatValue = TypeUtils.castToFloat(value);
        return floatValue == null ? 0.0F : floatValue;
    }

    default Double getDoubleByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToDouble(value);
    }

    default double getDoubleValueByExpression(String key) {
        Object value = this.getByExpression(key);
        Double doubleValue = TypeUtils.castToDouble(value);
        return doubleValue == null ? 0.0D : doubleValue;
    }

    default BigDecimal getBigDecimalByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToBigDecimal(value);
    }

    default BigInteger getBigIntegerByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToBigInteger(value);
    }

    default String getStringByExpression(String key) {
        Object value = this.getByExpression(key);
        return value == null ? null : value.toString();
    }

    default Date getDateByExpression(String key) {
        Object value = this.getByExpression(key);
        return TypeUtils.castToDate(value);
    }
}

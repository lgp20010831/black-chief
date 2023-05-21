package com.black.graphql.core.request.param;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class RequestParameter extends HashMap<String, Object> {
    private RequestParameter() {
    }

    public RequestParameter addParameter(String key, Object obj) {
        this.put(key, obj);
        return this;
    }

    public RequestParameter addObjectParameter(String key, Object obj) {
        if (obj instanceof RequestObjectParameter) {
            this.put(key, obj);
        } else {
            this.put(key, new RequestObjectParameter(obj));
        }

        return this;
    }

    public static RequestParameter buildByMap(Map map) {
        RequestParameter requestParameter = build();
        requestParameter.putAll(map);
        return requestParameter;
    }

    public static RequestParameter build() {
        RequestParameter requestParameter = new RequestParameter();
        return requestParameter;
    }

    public String toString() {
        Set<String> keys = this.keySet();
        if (keys.size() == 0) {
            return "";
        } else {
            String stringVal = "(";
            char connect = ',';

            String key;
            for(Iterator var4 = keys.iterator(); var4.hasNext(); stringVal = stringVal + key + ":" + this.packVal(this.get(key)) + connect) {
                key = (String)var4.next();
            }

            char last = stringVal.charAt(stringVal.length() - 1);
            if (connect == last) {
                stringVal = stringVal.substring(0, stringVal.length() - 1);
            }

            stringVal = stringVal + ")";
            return stringVal;
        }
    }

    private String packVal(Object val) {
        if (val == null) {
            return "null";
        } else if (!(val instanceof Integer) && !(val instanceof Boolean) && !(val instanceof Float) && !(val instanceof Double)) {
            if (val instanceof Enum) {
                Enum enumVal = (Enum)val;
                String enumName = enumVal.name();
                return enumName;
            } else {
                return val instanceof RequestObjectParameter ? val.toString() : "\\\"" + String.valueOf(val) + "\\\"";
            }
        } else {
            return String.valueOf(val);
        }
    }
}

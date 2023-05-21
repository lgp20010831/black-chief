package com.black.graphql.core.request.param;

import com.alibaba.fastjson.JSON;

@SuppressWarnings("all")
public class RequestObjectParameter {
    private Object data;

    public RequestObjectParameter(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String toString() {
        String json = this.dataToJson();
        json = json.replaceAll("\\\"", "\\\\\"");
        return json;
    }

    private String dataToJson() {
        return JSON.toJSONString(getData()).replaceAll("\"(\\w+)\"(\\s*:\\s*)","$1$2");
    }
}

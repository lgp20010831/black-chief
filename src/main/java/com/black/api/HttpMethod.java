package com.black.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.black.core.json.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Getter @Setter @SuppressWarnings("all")
public class HttpMethod {

    public static boolean showDian = false;
    public static int len = 30;
    List<String> requestMethod; List<String> requestUrl;
    Map<String, String> headers = new LinkedHashMap<>();
    //List<HttpMethodParam> params;
    String remark;
    String requestDome;
    String responseDome;
    String sort;
    String requestMethodString;
    String requestUrlString;
    boolean mulitPartRequest = false;
    //可执行的请求示例
    String requestInvokeDome;
    JSON requestJSON;
    String bgColor;
    String bgBorderColor;

    public void setHeaders(Map<String, String> headers) {
        if (headers != null){
            this.headers.putAll(headers);
        }
    }

    public void setRequestMethod(List<String> requestMethod) {
        this.requestMethod = requestMethod;
        StringJoiner joiner = new StringJoiner("/");
        for (String m : requestMethod) {
            joiner.add(m);
        }
        requestMethodString = joiner.toString();
        if (requestMethodString.startsWith("GET")){
            bgColor = "rgba(97,175,254,.1)";
            bgBorderColor = "#61affe";
        }else if (requestMethodString.startsWith("POST")){
            bgColor = "rgba(73,204,144,.1)";
            bgBorderColor = "#49cc90";
        }
    }

    public void setRequestUrl(List<String> requestUrl) {
        this.requestUrl = requestUrl;
        StringJoiner joiner = new StringJoiner(" or ");
        for (String m : requestUrl) {
            joiner.add(m);
        }
        requestUrlString = joiner.toString();
        if (requestUrlString.length() > len && showDian){
            requestUrlString = requestUrlString.substring(0, len + 1);
            requestUrlString = requestUrlString + "......";
        }
    }

    public String getJson(){
        return JsonUtils.letJson(this).toString(SerializerFeature.WriteMapNullValue);
    }
}

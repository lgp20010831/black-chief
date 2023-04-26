package com.black.api;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public abstract class AbstractApiParser implements ApiRequestAndResponseResolver{


    protected void handleHeaders(ClassWrapper<?> cw, HttpMethod method, String[] headers,
                                 Configuration configuration, MethodWrapper methodWrapper){

        processorHeader(method, headers);
        ApiHeaders annotation = cw.getAnnotation(ApiHeaders.class);
        if (annotation != null){
            processorHeader(method, annotation.value());
        }
        Map<String, String> globalHeaders = configuration.getGlobalHeaders();

        if (ApiV2Utils.isMuiltPartRequest(method, methodWrapper)){
            globalHeaders.put("Content-Type", "multipart/form-data");
        }
        method.setHeaders(globalHeaders);
    }


    protected void processorHeader(HttpMethod method, String[] headerText){
        Map<String, String> headers = new HashMap<>();
        for (String header : headerText) {
            String[] split = header.split(":");
            if (split.length == 2){
                headers.put(split[0].trim(), split[1].trim());
            }
        }
        method.setHeaders(headers);
    }

    protected static void setRemarkOfMethod(MethodWrapper mw, HttpMethod method, String defaultRemark){
        if (StringUtils.hasText(defaultRemark)){
            method.setRemark(defaultRemark);
        }else {
            List<String> requestUrl = method.getRequestUrl();
            StringJoiner joiner = new StringJoiner(" or ");
            for (String url : requestUrl) {
                joiner.add(url);
            }
            method.setRemark("url: " + joiner.toString());
        }
    }
}

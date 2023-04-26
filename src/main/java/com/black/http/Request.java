package com.black.http;

import com.alibaba.fastjson.JSONObject;
import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter @ToString
public class Request {

    private String httpId;

    /**

     * 请求方法 GET/POST/PUT/DELETE/OPTION...

     */

    private String method;

    /**

     * 请求的uri

     */

    private String uri;

    /**

     * http版本

     */

    private String version;

    /**

     * 请求头

     */

    private Map<String, String> headers;

    /**

     * 请求参数相关

     */

    private String message;


    /*
        调度地址
     */
    private String servletPath;

    /*
        url 参数列表
     */
    private String urlParamString;


    private JSONObject urlParamJson = new JSONObject();

    /*
        url 参数 map
     */
    private Map<String, String> urlParamMap = new LinkedHashMap<>();

    public void setUri(String uri) {
        this.uri = uri;

        String[] uris = uri.split("\\?");
        if (uris.length > 2) throw new IllegalStateException("ill state: " + uri);
        servletPath = StringUtils.removeIfStartWith(uris[0], "/");
        urlParamString = uris.length == 1 ? "" : uris[1];
        String[] params = urlParamString.split("&");
        for (String param : params) {
            if (StringUtils.hasText(param)){
                String[] kv = StringUtils.split(param, "=", 2, "ill param style:" + param);
                urlParamMap.put(kv[0], kv[1]);
                urlParamJson.put(kv[0], kv[1]);
            }
        }
    }
}

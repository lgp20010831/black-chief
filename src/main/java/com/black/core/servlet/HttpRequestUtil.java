package com.black.core.servlet;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public final class HttpRequestUtil {

    public static final String REQUEST_TOKEN_PARAM = "token$$validator";

    public static final String CORE_ORIGIN = "Origin";

    public static final String CORS_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

    public static final String HTTP_OPTIONS = "OPTIONS";

    public static final String CORS_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static boolean isCrossRequest(HttpServletRequest request){
        return  "OPTIONS".equals(request.getMethod()) &&
                (request.getHeader("Origin") != null ||
                 request.getHeader("Access-Control-Request-Mehtod") != null);
    }

    /**
     * @Description: 获取客户端IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if("127.0.0.1".equals(ip)){
                //根据网卡取本机配置的IP
                InetAddress inet=null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                ip= inet.getHostAddress();
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ip != null && ip.length() > 15){
            if(ip.indexOf(",")>0){
                ip = ip.substring(0,ip.indexOf(","));
            }
        }
        return ip;
    }

    public static void retrieval(JSONObject body, String... keys){
        for (String key : keys) {
            if (!body.containsKey(key)){
                throw new RuntimeException("缺少:" + key);
            }
        }
    }

    public static String singtonRetrieval(JSONObject body, String key){
        if (!body.containsKey(key)){
            throw new RuntimeException("缺少:" + key);
        }
        return body.getString(key);
    }

    public static String singtonIllNullRetrieval(JSONObject body, String key){
        String retrieval = singtonRetrieval(body, key);
        if (retrieval == null){
            throw new RuntimeException(key + " 不能为空");
        }
        return retrieval;
    }

    public static void ifIll(boolean result, String message){
        if (!result){
            throw new RuntimeException(message);
        }
    }

    public interface GetNotNullString{
        String get();
    }

    public static String illNull(GetNotNullString gns){
        return illNull(gns, "");
    }

    public static String illNull(GetNotNullString gns, String message){
        String result = gns.get();
        if (result == null){
            throw new RuntimeException(message);
        }
        return result;
    }

    public static Object caseIll(boolean result, String message, Object rv){
        if (!result){
            throw new RuntimeException(message);
        }
        return rv;
    }

    public static Object caseIll(boolean result, String message){
        return caseIll(result, message, null);
    }
}

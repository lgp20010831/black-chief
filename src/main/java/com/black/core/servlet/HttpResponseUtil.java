package com.black.core.servlet;

import com.alibaba.fastjson.JSON;
import com.black.core.builder.Col;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ResponseFacade;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class HttpResponseUtil {

    public static final String UTF_8_CHARACTER = "UTF-8";

    public static final String DECODE_TEXT_UTF_8_TYPE = "text/html; charset=utf-8";
    public static final String DECODE_JSON_UTF_8_TYPE = "application/json; charset=utf-8";
    //Access-Control-Allow-Origin=*, Access-Control-Allow-Methods=POST, Vary=Origin,
    // Access-Control-Max-Age=1800, Access-Control-Allow-Headers=authorization, content-type
    public static final Map<String, String> CORS_ALLOW_RESPONSE_HEADER
            = Col.of("Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "POST",
            "Vary", "Origin", "Access-Control-Max-Age", "1800",
            "Access-Control-Allow-Headers", "authorization, content-type");

    public static final int OK = 200;

    public static final int CORS_FAIL = 403;

    public static void setResponseTextType(HttpServletResponse response){
        response.setCharacterEncoding(UTF_8_CHARACTER);
        response.setContentType(DECODE_TEXT_UTF_8_TYPE);
    }

    public static void setResponseJsonType(HttpServletResponse response){
        response.setCharacterEncoding(UTF_8_CHARACTER);
        response.setContentType(DECODE_JSON_UTF_8_TYPE);
    }

    public static Map<String, String> getResponseHeadersMap(HttpServletResponse response){
        Collection<String> headerNames = response.getHeaderNames();
        Map<String, String> result = new HashMap<>();
        for (String headerName : headerNames) {
            result.put(headerName, response.getHeader(headerName));
        }
        return result;
    }

    public static void writeUtf8JsonResult(Object result ,HttpServletResponse response){
        response.setContentType("application/json;charset=utf-8");
        writeResult(JSON.toJSON(result), response);
    }

    public static void writeResult(Object result ,HttpServletResponse response){
        if (response instanceof ResponseFacade){
            ResponseFacade responseFacade = (ResponseFacade) response;
            PrintWriter writer;
            try {
                writer = responseFacade.getWriter();
                writer.print(result);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("写入响应数据时发生异常");
                }
                CentralizedExceptionHandling.handlerException(e);
            }
        }
    }
}

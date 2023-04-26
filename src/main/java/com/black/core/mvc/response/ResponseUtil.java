package com.black.core.mvc.response;

import lombok.extern.log4j.Log4j2;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.black.core.response.Code.*;
import static com.black.core.response.VariablePool.*;

@Log4j2
public final class ResponseUtil {

    public static Response ok(Object result){
        return new Response(SUCCESS.value(), true, WORK_SUCCESSFUL, result);
    }

    public static Response ok(){
        return new Response(SUCCESS.value(), true, WORK_SUCCESSFUL);
    }


    public static Response fail(){
        return new Response(HANDLER_FAIL.value(), false, WORK_FAIL);
    }

    public static Response invalid(){
        return new Response(TOKEN_INVALID.value(), false, TOKEN_INVAILD);
    }

    public static void configResponse(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        fileName = URLEncoder.encode(fileName,"UTF-8");
        response.reset();
        String contentType = new MimetypesFileTypeMap().getContentType(fileName);
        log.info("fileName: {} ---> content-type: {}", fileName, contentType);
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ";charset=UTF-8");
    }


}

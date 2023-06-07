package com.black.fun_net;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.mvc.response.ResponseUtil;
import com.black.core.util.BaseController;
import com.black.sql_v2.period.ListResourceHandler;
import com.black.utils.TypeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public abstract class Net extends BaseController implements ListResourceHandler {

    private final ThreadLocal<Object> bodyLocal = new ThreadLocal<>();

    private final ThreadLocal<Object> resultLocal = new ThreadLocal<>();

    public HttpServletRequest request(){
        return getRequest();
    }

    public HttpServletResponse response(){
        return getResponse();
    }

    public OutputStream getOutputStream() throws IOException {
        return response().getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return request().getInputStream();
    }

    public void configResponse(String fileName) throws UnsupportedEncodingException {
        ResponseUtil.configResponse(response(), fileName, false);
    }

    public void setBody(Object body){
        bodyLocal.set(body);
    }

    public JSONObject body(){
        Object body = bodyLocal.get();
        return convert(body, JSONObject.class);
    }

    public JSONArray array(){
        Object body = bodyLocal.get();
        return convert(body, JSONArray.class);
    }

    @Override
    public List<Map<String, Object>> list() {
        return listBody();
    }

    public List<Map<String, Object>> listBody(){
        Object body = bodyLocal.get();
        return convert(body, List.class);
    }

    public List<String> listStr(){
        Object body = bodyLocal.get();
        return convert(body, List.class);
    }

    public String text(){
        Object body = bodyLocal.get();
        return TypeUtils.castToString(body);
    }

    public void write(Object result){
        resultLocal.set(result);
    }


    public Object obtainWriteResult(){
        return resultLocal.get();
    }

    protected void fetchFinishCallback(){
        bodyLocal.remove();
        resultLocal.remove();
    }

}

package com.black;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.arg.ArgWrapper;
import com.black.core.mvc.response.ResponseUtil;
import com.black.core.util.Assert;
import com.black.core.util.BaseController;
import com.black.fun_net.RequestParamIndexHandler;
import com.black.sql_v2.period.ListResourceHandler;
import com.black.utils.CollectionUtils;
import com.black.utils.LocalMap;
import com.black.utils.TypeUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-27 17:04
 */
@SuppressWarnings("all")
public abstract class Servlet extends BaseController implements ListResourceHandler,
        RequestParamIndexHandler {


    private final ThreadLocal<Object> bodyLocal = new ThreadLocal<>();

    private final LocalMap<String, Object> partLocal = new LocalMap<>();

    private final ThreadLocal<ArgWrapper> argLocal = new ThreadLocal<>();

    public HttpServletRequest request(){
        return getRequest();
    }

    public HttpServletResponse response(){
        return getResponse();
    }

    public String getNonNullHeader(String name){
        return Assert.nonNull(getHeader(name), "header: " + name + " is null");
    }

    public String getHeader(String name){
        return getRequest().getHeader(name);
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

    public void setArg(Object[] args){
        argLocal.set(new ArgWrapper(args));
    }

    public void setBody(Object body){
        bodyLocal.set(body);
    }

    public void setPart(Map<String, Object> partMap){
        partLocal.putAll(partMap);
    }

    public MultipartFile file(){
        return (MultipartFile) CollectionUtils.firstElement(partLocal);
    }

    public MultipartFile file(String name){
        return (MultipartFile) partLocal.get(name);
    }

    public List<MultipartFile> files(){
        return (List<MultipartFile>) CollectionUtils.firstElement(partLocal);
    }

    public List<MultipartFile> files(String name){
        return (List<MultipartFile>) partLocal.get(name);
    }

    public String stringPart(String name){
        return convert(partLocal.get(name), String.class);
    }

    public Integer intPart(String name){
        return convert(partLocal.get(name), Integer.class);
    }

    public Long longPart(String name){
        return convert(partLocal.get(name), Long.class);
    }

    public Double doublePart(String name){
        return convert(partLocal.get(name), Double.class);
    }

    public Short shortPart(String name){
        return convert(partLocal.get(name), Short.class);
    }

    public Float floatPart(String name){
        return convert(partLocal.get(name), Float.class);
    }

    public Boolean boolPart(String name){
        return convert(partLocal.get(name), Boolean.class);
    }

    public JSONObject jsonPart(String name){
        return convert(partLocal.get(name), JSONObject.class);
    }

    public JSONArray arrayPart(String name){
        return convert(partLocal.get(name), JSONArray.class);
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


    public ArgWrapper argWrapper(){
        return argLocal.get();
    }

    public String $1(){
        return argWrapper().getString(0);
    }

    public <T> T $1(Class<T> type){
        return argWrapper().getObject(0, type);
    }

    public String $2(){
        return argWrapper().getString(1);
    }

    public <T> T $2(Class<T> type){
        return argWrapper().getObject(1, type);
    }

    public String $3(){
        return argWrapper().getString(2);
    }

    public <T> T $3(Class<T> type){
        return argWrapper().getObject(2, type);
    }

    public String $4(){
        return argWrapper().getString(3);
    }

    public <T> T $4(Class<T> type){
        return argWrapper().getObject(3, type);
    }

    public String $5(){
        return argWrapper().getString(4);
    }

    public <T> T $5(Class<T> type){
        return argWrapper().getObject(4, type);
    }

    public void fetchFinishCallback(){
        bodyLocal.remove();
        argLocal.remove();
        partLocal.removeCurrent();
    }

    @Override
    public Map<String, Object> arrangeParamMap() {
        return getFormData();
    }


}

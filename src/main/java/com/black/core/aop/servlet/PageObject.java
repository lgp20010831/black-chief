package com.black.core.aop.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.Convert;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PageObject<T> {

    private Page<Object> page;

    private Object source;

    private Long total;

    private HttpMethodWrapper wrapper;

    private final Map<String, T> transitionMap = new ConcurrentHashMap<>();

    private final String DEFAULT_ALIAS = "name-page1";

    public PageObject<T> transition(T result){
        return transition(result, DEFAULT_ALIAS);
    }

    public PageObject<T> transition(T result, String alias){
        transitionMap.put(alias, result);
        return this;
    }

    public PageObject<T> echoSave(TransitionEcho<T> echo){
        return echoSave(echo, DEFAULT_ALIAS);
    }

    public PageObject<T> echoSave(TransitionEcho<T> echo, String alias){
        return echoSave(echo, alias, DEFAULT_ALIAS);
    }

    public PageObject<T> echoSave(TransitionEcho<T> echo, String alias, String saveAlias){
        Object aDo = echo.transitionDo(transitionMap.get(alias));
        transitionMap.put(saveAlias, (T) aDo);
        return this;
    }

    public PageObject<T> echoResult(TransitionEcho<T> echo){
        return echoResult(echo, DEFAULT_ALIAS);
    }

    public PageObject<T> echoResult(TransitionEcho<T> echo, String alias){
        return setSource(echo.transitionDo(transitionMap.get(alias)));
    }

    public PageObject<T> endSource(){
        return endSource(DEFAULT_ALIAS);
    }

    public PageObject<T> endSource(String alias){
        return setSource(transitionMap.get(alias));
    }

    public PageObject(){}

    public PageObject(Page<Object> page, HttpMethodWrapper wrapper) {
        this.page = page;
        this.wrapper = wrapper;
    }

    public Object getSource() {
        return source;
    }

    public PageObject<T> doPage(){
        String pageNumArgName = wrapper.getPageNumArgName();
        String pageSizeArgName = wrapper.getPageSizeArgName();
        Integer pageSize = null, pageNum = null;
        if (wrapper.isJsonRequest()) {
            Parameter[] parameters;
            for (int i = 0; i < (parameters = wrapper.getHttpMethod().getParameters()).length; i++) {
                if (AnnotationUtils.getAnnotation(parameters[i], RequestBody.class) != null) {
                    try {
                        Object wrapperArg = wrapper.getArgs()[i];
                        if (wrapperArg == null) {
                            return this;
                        }
                        JSONObject body;
                        if (wrapperArg instanceof JSONObject) {
                            body = (JSONObject) wrapperArg;
                        } else {
                            body = JSON.parseObject(wrapperArg.toString());
                        }
                        pageNum = body.getInteger(pageNumArgName);
                        pageSize = body.getInteger(pageSizeArgName);
                        break;
                    } catch (Throwable e) {
                        CentralizedExceptionHandling.handlerException(e);
                    }
                }
            }
        } else {
            pageSize = Convert.toInt(AopControllerIntercept.getRequest().getParameter(wrapper.getPageSizeArgName()));
            pageNum = Convert.toInt(AopControllerIntercept.getRequest().getParameter(wrapper.getPageNumArgName()));
        }

        if (pageSize == null || pageNum == null) {
            return this;
        }
        doStartPage(pageSize, pageNum);
        return this;
    }

    private void doStartPage(Integer pageSize, Integer pageNum) {
        page = PageHelper.startPage(pageNum, pageSize);
    }

    public PageObject<T> setSource(Object source) {
        this.source = source;
        return this;
    }

    public boolean startPage(){
        return page != null;
    }

    public Page<Object> getPage() {
        return page;
    }

    public PageObject<T> totalNow(){
        if (startPage()){
            total = page.getTotal();
        }
        return this;
    }

    public PageObject<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Long getTotal() {
        return total;
    }
}

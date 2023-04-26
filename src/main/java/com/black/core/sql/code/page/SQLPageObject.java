package com.black.core.sql.code.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.aop.servlet.TransitionEcho;
import com.black.core.sql.annotation.OpenSQLAutonomyPaging;
import com.black.core.util.Convert;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLPageObject<T> {

    private Page<?> page;

    private Object source;

    private Long total;

    private HttpMethodWrapper wrapper;

    private final Map<String, T> transitionMap = new ConcurrentHashMap<>();

    private final String DEFAULT_ALIAS = "name-page1";

    public SQLPageObject<T> transition(T result){
        return transition(result, DEFAULT_ALIAS);
    }

    public SQLPageObject<T> transition(T result, String alias){
        transitionMap.put(alias, result);
        return this;
    }

    public SQLPageObject<T> echoSave(TransitionEcho<T> echo){
        return echoSave(echo, DEFAULT_ALIAS);
    }

    public SQLPageObject<T> echoSave(TransitionEcho<T> echo, String alias){
        return echoSave(echo, alias, DEFAULT_ALIAS);
    }

    public SQLPageObject<T> echoSave(TransitionEcho<T> echo, String alias, String saveAlias){
        Object aDo = echo.transitionDo(transitionMap.get(alias));
        transitionMap.put(saveAlias, (T) aDo);
        return this;
    }

    public SQLPageObject<T> echoResult(TransitionEcho<T> echo){
        return echoResult(echo, DEFAULT_ALIAS);
    }

    public SQLPageObject<T> echoResult(TransitionEcho<T> echo, String alias){
        return setSource(echo.transitionDo(transitionMap.get(alias)));
    }

    public SQLPageObject<T> endSource(){
        return endSource(DEFAULT_ALIAS);
    }

    public SQLPageObject<T> endSource(String alias){
        return setSource(transitionMap.get(alias));
    }

    public SQLPageObject(){}

    public SQLPageObject(HttpMethodWrapper httpMethodWrapper){
        this(null, httpMethodWrapper);
    }

    public SQLPageObject(Page<?> page, HttpMethodWrapper wrapper) {
        this.page = page;
        this.wrapper = wrapper;
    }

    public Object getSource() {
        return source;
    }

    public SQLPageObject<T> doPage(){
        Integer pageSize = null, pageNum = null;
        Object[] args = wrapper.getArgs();
        Method method = wrapper.getHttpMethod();
        OpenSQLAutonomyPaging sqlPage = AnnotationUtils.getAnnotation(method, OpenSQLAutonomyPaging.class);
        if (sqlPage != null){
            if (wrapper.isJsonRequest()) {
                ParameterWrapper parameter = wrapper.getSinglonParameterByAnnotation(RequestBody.class);
                if (parameter == null){
                    return this;
                }
                Object arg = args[parameter.getIndex()];
                if (arg != null){
                    JSONObject body;
                    if (arg instanceof JSONObject) {
                        body = (JSONObject) arg;
                    } else {
                        body = JSON.parseObject(arg.toString());
                    }
                    pageNum = body.getInteger(sqlPage.pageNum());
                    pageSize = body.getInteger(sqlPage.pageSize());
                }
            }else {
                pageSize = Convert.toInt(AopControllerIntercept.getRequest().getParameter(sqlPage.pageSize()));
                pageNum = Convert.toInt(AopControllerIntercept.getRequest().getParameter(sqlPage.pageNum()));
            }
            if (pageSize == null || pageNum == null) {
                return this;
            }
            doStartPage(pageSize, pageNum);
        }
        return this;
    }

    private void doStartPage(Integer pageSize, Integer pageNum) {
        page = PageHelper.openPage(pageNum, pageSize);
    }

    public SQLPageObject<T> setSource(Object source) {
        this.source = source;
        return this;
    }

    public boolean isOpenPage(){
        return page != null;
    }

    public Page<?> getPage() {
        return page;
    }

    public SQLPageObject<T> totalNow(){
        if (isOpenPage()){
            total = (long) page.getTotal();
        }
        return this;
    }

    public SQLPageObject<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Long getTotal() {
        return total;
    }
}

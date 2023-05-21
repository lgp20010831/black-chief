package com.black.core.util;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.sql_v2.Opt;
import com.black.sql_v2.Sql;
import com.black.standard.AttributeMapHandler;
import com.black.standard.SqlOperator;
import com.black.standard.TypeConvertStandard;
import com.black.standard.XmlSqlOperator;
import com.black.xml.XmlSql;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class BaseController implements BeanFactoryAware, SqlOperator, Opt,
        XmlSqlOperator, AttributeMapHandler, TypeConvertStandard {


    protected BeanFactory factory;
    protected final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    protected <M> M getBean(@NonNull Class<M> beanType){
        Assert.notNull(factory, "factory 异常为空");
        if (cache.containsKey(beanType)){
            return (M) cache.get(beanType);
        }
        try {
            M bean = factory.getBean(beanType);
            cache.put(beanType, bean);
            return bean;
        }catch (BeansException be){
            throw new IllegalStateException("无法获取 bean 对象: " + beanType.getSimpleName());
        }
    }

    public HttpServletRequest getRequest(){
        return AopControllerIntercept.getRequest();
    }

    public HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public String getPathPart(int index){
        String servletPath = getRequest().getServletPath();
        String[] splits = servletPath.split("/");
        return splits[index];
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        factory = beanFactory;
    }

    public BeanFactory getFactory() {
        return factory;
    }

    @Override
    public String getAlias() {
        return Sql.DEFAULT_ALIAS;
    }

    @Override
    public SqlOperator getSqlDelegate() {
        return Sql.opt(getAlias());
    }

    @Override
    public XmlSqlOperator getSqlXmlDelegate() {
        return XmlSql.opt(getAlias());
    }

    @Override
    public Map<String, Object> getFormData() {
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        Map<String, String[]> map = getRequest().getParameterMap();
        map.forEach((k, v) -> {
            Object value;
            if (v == null){
                value = null;
            }else if (v.length == 1){
                value = v[0];
            }else {
                value = Av0.as(v);
            }
            linkedHashMap.put(k, value);
        });
        return linkedHashMap;
    }
}

package com.black.api;


import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.datasource.ProduceElementDataSourceDiscriminateManager;
import com.black.core.annotation.Sort;
import com.black.core.api.ApiUtil;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

@Log4j2
public class HttpApiParser {

    private final Configuration configuration;

    public HttpApiParser(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<HttpMethod> parse(){
        Map<Class<?>, Set<MethodWrapper>> setMap = configuration.getApiMethods();
        List<HttpMethod> methods = new ArrayList<>();
        for (Class<?> type : setMap.keySet()) {
            Set<MethodWrapper> apiMethods = setMap.get(type);
            for (MethodWrapper apiMethod : apiMethods) {
                HttpMethod httpMethod = null;
                try {
                    httpMethod = parseMethod(apiMethod, type);
                } catch (SQLException e) {
                    continue;
                }
                methods.add(httpMethod);
            }
        }
        return methods;
    }

    private List<Class<?>> sortController(Collection<Class<?>> types){
        ArrayList<Class<?>> list = new ArrayList<>(types);
        return ServiceUtils.sort(list, clazz -> {
            String remark = getControllerRemark(clazz);
            if (remark.startsWith("接口模块: ")){
                remark = clazz.getSimpleName();
            }
            return remark;
        }, true);
    }

    public static boolean isOverwrite(MethodWrapper mw, Class<?> type){
        Class<?> declaringClass = mw.getDeclaringClass();
        if (declaringClass.equals(type)){
            return false;
        }
        boolean superMethod = declaringClass.isAssignableFrom(type);
        if (!superMethod){
            return false;
        }
        String name = mw.getName();
        ClassWrapper<?> classWrapper = ClassWrapper.get(type);
        List<MethodWrapper> expectMethod = classWrapper.getExpectMethod(name);
        for (MethodWrapper methodWrapper : expectMethod) {
            if (methodWrapper.getDeclaringClass().equals(type)
                    && methodWrapper.getParameterCount() == mw.getParameterCount()) {
                return true;
            }
        }
        return false;
    }

    public List<HttpModular> parseModular(){
        Map<Class<?>, Set<MethodWrapper>> setMap = configuration.getApiMethods();
        List<HttpModular> modulars = new ArrayList<>();
        int s = 0;
        List<Class<?>> classes = sortController(setMap.keySet());
        for (Class<?> type : classes) {
            HttpModular modular = new HttpModular();
            List<HttpMethod> methods = new ArrayList<>();
            Set<MethodWrapper> apiMethods = setMap.get(type);
            List<MethodWrapper> methodWrappers = sortMethods(apiMethods);
            modular.setSort(++s);
            int sort = 0;
            for (MethodWrapper apiMethod : methodWrappers) {
                try {

                    if(isOverwrite(apiMethod, type)){
                        continue;
                    }

                    if (isSkipMethod(apiMethod, type)) {
                        continue;
                    }

                    HttpMethod httpMethod = parseMethod(apiMethod, type);
                    if (httpMethod != null){
                        httpMethod.setSort(s + "." + ++sort);
                        methods.add(httpMethod);
                    }
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    if (log.isInfoEnabled()) {
                        log.info("解析 http method :[{}] 错误: {}", apiMethod.getMethod(), e.getMessage());
                    }
                }
            }
            modular.setMethods(methods);
            modular.setModularRemark(getControllerRemark(type));
            if (methods.size() > 0){
                modulars.add(modular);
            }else {
                s--;
            }
        }
        return modulars;
    }

    protected boolean isSkipMethod(MethodWrapper apiMethod, Class<?> type){
        if (ApiSkipConfigurer.class.isAssignableFrom(type)){
            Object instance = InstanceBeanManager.instance(type, InstanceType.BEAN_FACTORY);
            ApiSkipConfigurer skipConfigurer = (ApiSkipConfigurer) instance;
            String[] skips = skipConfigurer.skips();
            if (skips != null){
                String name = apiMethod.getName();
                for (String skip : skips) {
                    if (name.equals(skip)){
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public List<MethodWrapper> sortMethods(Set<MethodWrapper> apiMethods){
        return ServiceUtils.sort(new ArrayList<>(apiMethods), methodWrapper -> {
            Sort annotation = methodWrapper.getAnnotation(Sort.class);
            return annotation == null ? methodWrapper.getName() : annotation.value();
        }, true);

    }

    public String getControllerRemark(Class<?> type){
        String reamrk = null;
        for (Function<Class<?>, String> function : Configuration.getControllerRemarkFunList) {
            reamrk = function.apply(type);
            if (reamrk != null){
                break;
            }
        }
        return StringUtils.hasText(reamrk) ? reamrk : "接口模块: [" + type.getSimpleName() + "]";
    }

    public HttpMethod parseMethod(MethodWrapper wrapper, Class<?> type) throws SQLException {
        Connection protogenesisConnection = configuration.getConnection();
        Connection tempConnection = null;
        try {
            DataSource dataSource = ProduceElementDataSourceDiscriminateManager.tryGetDataSource(protogenesisConnection);
            if (dataSource != null){
                tempConnection = dataSource.getConnection();
                //切换connection
                configuration.setConnection(tempConnection);
            }
            HttpMethod method = new HttpMethod();
            Class<?> declaringClass = wrapper.getDeclaringClass();

            //获取请求url
            List<String> methodUrls = ApiUtil.getMethodUrls(wrapper.getMethod(), type);
            method.setRequestUrl(methodUrls);

            //获取请求方法
            List<String> httpMethod = ApiUtil.getMethodHttpMethod(type, wrapper.getMethod());
            method.setRequestMethod(httpMethod);

            ApiRequestAndResponseResolver apiResolver = null;
            for (ApiRequestAndResponseResolver resolver : configuration.getApiRequestAndResponseResolvers()) {
                if (resolver.support(wrapper)) {
                    apiResolver = resolver;
                    break;
                }
            }
            if (apiResolver == null){
                return null;
            }
            apiResolver.doResolve(wrapper, method, configuration, type);
            return method;
        }finally {
            configuration.setConnection(protogenesisConnection);
            SQLUtils.closeConnection(tempConnection);
        }
    }


}

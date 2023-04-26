package com.black.core.api.tacitly;

import com.black.core.api.ApiHttpRestConfigurer;
import com.black.core.api.ApiResponseHolder;
import com.black.core.api.ApiWrapperFactory;
import com.black.core.api.handler.*;
import com.black.core.api.pojo.ApiController;
import com.black.core.api.pojo.ApiParameterDetails;
import com.black.core.api.pojo.ApiRestInterface;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.spring.util.ApplicationUtil;
import com.black.utils.NameUtil;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class AnalysisProcessActuator {

    private final ApiAliasManger aliasManger;

    private final ApiMethodManger methodManger;

    private final InstanceFactory instanceFactory;

    private final ApiDependencyManger dependencyManger;

    private final ApiWrapperFactory wrapperFactory = new ApiWrapperFactory();

    /**
     * key = 实体类 class 对象
     * value = 字段 map
     *          key = 字段名
     *          value = 类型
     */
    private final Map<Class<?>, Map<String, String>> paramCache = new HashMap<>();

    private final Map<String, Map<String, String>> paramAliasCache = new HashMap<>();
    /**
     * key = 实体类  class 对象
     * value = 字段封装对象集合
     */
    private final Map<Class<?>, List<ApiParameterDetails>> parameterDetailsMap = new HashMap<>();

    private final Map<String, List<ApiParameterDetails>> parameterDetailsAliasMap = new HashMap<>();

    private final ExampleStreamAdapter responseExamplerReader;

    private final JsonRequestExampleReader jsonRequestExampleReader;

    private final FormDataRequestExampleReader formDataRequestExampleReader;

    private final ApiHttpRestConfigurer configurer;

    /** 接口方法基本信息获取类 */
    private ApiInterfaceBaseHandler apiInterfaceBaseHandler;

    /** 请求实例 reader */
    private Collection<RequestExampleReader> requestExampleReaders;

    /** 请求头处理类 */
    private Collection<RequestHeadersHandler> requestHeadersHandlers;

    /** 响应实例处理类 */
    private Collection<ResponseExampleReader>  responseExampleReaders;

    /** 请求参数处理类 */
    private Collection<RequestParamHandler>  requestParamHandlers;

    public AnalysisProcessActuator(ApiAliasManger aliasManger,
                                   ApiMethodManger methodManger, ApiDependencyManger dependencyManger,
                                   InstanceFactory instanceFactory, ApiHttpRestConfigurer configurer) {
        this.aliasManger = aliasManger;
        this.methodManger = methodManger;
        this.dependencyManger = dependencyManger;
        jsonRequestExampleReader = new JsonRequestExampleReader(aliasManger, this);
        responseExamplerReader = new TacitlyExampleStreamAdapter(aliasManger, dependencyManger);
        ((TacitlyExampleStreamAdapter)responseExamplerReader).setRequestBufferExampleReader(jsonRequestExampleReader);
        formDataRequestExampleReader = new FormDataRequestExampleReader(aliasManger, this);
        this.instanceFactory = instanceFactory;
        this.configurer = configurer;
    }

    //入参, 控制器 class 对象, 和全局依赖实体类对象
    public ApiController invokeBuilderSource(Class<?> controllerClass, Collection<Class<?>> dependencyClasses){
        //解析实体类然后去填充
        //paramCache
        //parameterDetailsMap
        handlerCache(dependencyClasses, controllerClass);

        //控制器依赖的实体类
        List<Class<?>> dependPojoClass = dependencyManger.getDependencyMap().get(controllerClass);

        //控制器对应的有效方法
        List<Method> methods = methodManger.getCollectApiMethods().get(controllerClass);

        //控制器别名
        String alias = NameUtil.getName(controllerClass);


        List<ApiRestInterface> restInterfaces = new ArrayList<>();
        if (methods != null){

            for (Method method : methods) {

                //获取方法 requestItemModule, responseItemModule
                ItemResolutionModule requestItemModule = methodManger.getRequestResolutionModuleMap().get(method);
                ItemResolutionModule responseItemModule = methodManger.getResponseResolutionModuleMap().get(method);
                //尝试获取方法的注释
                String remark = apiInterfaceBaseHandler.getMethodRemark(controllerClass, method, alias);

                //获取方法的 http 方法
                List<String> httpMethod = apiInterfaceBaseHandler.getMethodHttpMethod(controllerClass, method, alias);

                //获取方法的所有请求 url
                List<String> httpUrls = apiInterfaceBaseHandler.getMethodHttpUrls(controllerClass, method, alias);

                //获取方法的请求头
                Map<String, String> headers = new HashMap<>();
                for (RequestHeadersHandler headersHandler : requestHeadersHandlers) {
                    headersHandler.obtainRequestHeaders(controllerClass, method, alias, headers);
                }

                List<ApiParameterDetails> detailsList = new ArrayList<>();
                for (Class<?> pojoClass : dependPojoClass) {
                       detailsList.addAll(parameterDetailsMap.get(pojoClass));
                }

                //克隆了一份字段封装集合
                List<ApiParameterDetails> clone = (List<ApiParameterDetails>) ApplicationUtil.clone(detailsList);
                //将克隆后的 api 字段暴露在外面, 进行添加或者删除
                for (RequestParamHandler paramHandler : requestParamHandlers) {
                    paramHandler.filterMethodParams(controllerClass, method, clone, dependPojoClass, wrapperFactory);
                }

                //生成请求示例
                Boolean isJson;
                String requestStream;
                Map<String , String> params;
                if (isJson = headers.containsValue("application/json")) {
                    requestStream = jsonRequestExampleReader.writeStream(params = convert(clone));
                }else {
                    requestStream = formDataRequestExampleReader.writeStream(params = convert(clone));
                }
                ExampleStreamAdapter exampleStreamAdapter = obtainExampleStreamAdapter(clone, isJson);
                for (RequestExampleReader exampleReader : requestExampleReaders) {
                    requestStream = exampleReader.handlerRequestParamExample(clone, requestStream, exampleStreamAdapter);
                }

                //最后一步生成响应实例
                String responseExample = "";
                responseExamplerReader.clear();
                for (ResponseExampleReader exampleReader : responseExampleReaders) {
                    responseExample = exampleReader.handlerApiResponseExample(ApiResponseHolder.getApiResponseClass(),
                            params, method, controllerClass, dependPojoClass, responseExample, responseExamplerReader);
                }
                restInterfaces.add(wrapperFactory.createRestInterface(remark,
                        httpUrls, httpMethod, headers, clone, requestStream, responseExample));
            }
            return wrapperFactory.createControllerWrapper(configurer.getControllerClassRemrk(controllerClass), restInterfaces);
        }
        return null;
    }

    protected ExampleStreamAdapter obtainExampleStreamAdapter(List<ApiParameterDetails> details, boolean isJson){
        TacitlyExampleStreamAdapter adapter = instanceFactory.getInstance(TacitlyExampleStreamAdapter.class);
        adapter.setApiParameterDetails(details);
        if (isJson){
            adapter.setRequestBufferExampleReader(jsonRequestExampleReader);
        }else {
            adapter.setRequestBufferExampleReader(formDataRequestExampleReader);
        }
        return adapter;
    }

    protected void handlerCache(Collection<Class<?>> dependencyClasses, Class<?> controllerClass){
        //去解析实体类
        for (Class<?> filterPojoClass : dependencyClasses) {

            //如果缓存不存在, 代表此实体类还没有解析过
            if (!paramCache.containsKey(filterPojoClass)){

                //key = 字段名, value = 字段类型
                Map<String, String> paramTypeMutes = new HashMap<>();

                //默认情况 annotation = ignore.class
                Class<? extends Annotation> annotation = null;
                for (RequestParamHandler paramHandler : requestParamHandlers) {
                    annotation = paramHandler.filterUselessParamsAnnoation(filterPojoClass);
                }

                //每个实体类字段的封装类的集合
                List<ApiParameterDetails> detailList = new ArrayList<>();
                gtu: for (Field field : ReflexHandler.getAccessibleFields(filterPojoClass)) {

                    //先判断字段上是否带有过滤注解
                    if(annotation != null){
                        if (AnnotationUtils.getAnnotation(field, annotation) != null) {
                            continue gtu;
                        }
                    }

                    //再次过滤时间字段和删除表示
                    for (RequestParamHandler paramHandler : requestParamHandlers) {
                        if (!paramHandler.filterRemainingParam(field.getType(), field, field.getName())) {
                            continue gtu;
                        }
                    }

                    //初步构造, 根据字段构造除 ApiParameterDetails 对象
                    ApiParameterDetails details = wrapperFactory.createParamDetails(field);

                    //进行了一此回调, 默认的情况下是去检查这个字段有没有带 apiRemark注解
                    //有的话将注释设置到 ApiParameterDetails 对象中
                    for (RequestParamHandler requestParamHandler : requestParamHandlers) {
                        requestParamHandler.postWrapper(controllerClass, field, details);
                    }
                    paramTypeMutes.put(field.getName(), field.getType().getSimpleName());
                    detailList.add(details);
                }
                parameterDetailsMap.put(filterPojoClass, detailList);
                paramCache.put(filterPojoClass, paramTypeMutes);
                String alias = aliasManger.queryPojoAlias(filterPojoClass);
                if (alias != null){
                    paramAliasCache.put(alias, paramTypeMutes);
                    parameterDetailsAliasMap.put(alias, detailList);
                }
            }
        }
    }

    private Map<String, String> convert(Collection<ApiParameterDetails> details){
        Map<String, String> map = new HashMap<>();
        for (ApiParameterDetails detail : details) {
            map.put(detail.getName(), detail.getType());
        }
        return map;
    }

    public Map<String, String> queryParamMap(Class<?> pojoClass){
        return paramCache.get(pojoClass);
    }

    public void setRequestParamHandlers(Collection<RequestParamHandler> requestParamHandlers) {
        this.requestParamHandlers = requestParamHandlers;
    }

    public void setApiInterfaceBaseHandlers(ApiInterfaceBaseHandler apiInterfaceBaseHandlers) {
        this.apiInterfaceBaseHandler = apiInterfaceBaseHandlers;
    }

    public void setRequestExampleReaders(Collection<RequestExampleReader> requestExampleReaders) {
        this.requestExampleReaders = requestExampleReaders;
    }

    public void setRequestHeadersHandlers(Collection<RequestHeadersHandler> requestHeadersHandlers) {
        this.requestHeadersHandlers = requestHeadersHandlers;
    }

    public void setResponseExampleReaders(Collection<ResponseExampleReader> responseExampleReaders) {
        this.responseExampleReaders = responseExampleReaders;
    }
}

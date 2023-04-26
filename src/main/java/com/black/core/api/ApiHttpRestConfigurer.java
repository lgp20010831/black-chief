package com.black.core.api;

import com.black.api.ApiRemark;
import com.black.core.api.annotation.DependencyBean;
import com.black.core.api.handler.*;
import com.black.core.builder.Col;
import com.black.core.mvc.response.Response;
import com.black.utils.NameUtil;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ApiHttpRestConfigurer {


   default String[] scannerPages(){
       return null;
   }

   String[] pojoScannerPackages();

   default String getThymeleafPath(){
       return "autoBuild/api/api1.0.txt";
   }

   default void handlerDependency(Class<?> controllerClass, String alias, Map<String, Class<?>> pojoMap, DependencyRegister register){
       DependencyBean bean = AnnotationUtils.getAnnotation(controllerClass, DependencyBean.class);
       if (bean != null){
           register.register(bean.value());
       }else {
           if (alias.contains("Controller")){
               register.register(pojoMap.get(alias.substring(0, alias.indexOf("Controller"))));
           }
       }
   }

   default void registerApiMethodFilter(Collection<ApiMethodFilter> apiMethodFilters){

   }

   default String getControllerClassRemrk(Class<?> controllerClass){
       String remark = NameUtil.getName(controllerClass);
       ApiRemark apiRemark = AnnotationUtils.getAnnotation(controllerClass, ApiRemark.class);
       if (apiRemark != null){
           remark = apiRemark.value();
       }
       return remark;
   }

   default ApiMethodCollector registerApiMethodCollector(){
       return controllerClass -> Col.of(controllerClass, ReflexHandler.getAccessibleMethods(controllerClass)
               .stream().filter(
                       m -> AnnotationUtils.getAnnotation(m, RequestMapping.class) != null
               ).collect(Collectors.toList()));
   }


   default Class<?> registerResponseClass(){
       return Response.class;
   }

   default ApiInterfaceBaseHandler registerApiInterfaceBaseHandlers(){
       return new ApiInterfaceBaseHandler() {
           @Override
           public String getMethodRemark(Class<?> controllerClass, Method method, String alias) {
               String methodName = method.getName();
               if (methodName.startsWith("query")){
                   return "查询数据接口";
               }else if (methodName.startsWith("insert")){
                   return "添加数据接口";
               }else if (methodName.startsWith("update")){
                   return "更新数据接口";
               }else if (methodName.startsWith("delete")){
                   return "删除数据接口";
               }
               return methodName + "功能接口";
           }

           @Override
           public List<String> getMethodHttpUrls(Class<?> controllerClass, Method method, String alias) {
               return ApiUtil.getMethodUrls(method, controllerClass);
           }

           @Override
           public List<String> getMethodHttpMethod(Class<?> controllerClass, Method method, String alias) {
               List<String> httpMethods = new ArrayList<>();
               if (AnnotationUtils.getAnnotation(method, GetMapping.class) != null){
                   httpMethods.add("GET");
               }else if (AnnotationUtils.getAnnotation(method, PostMapping.class) != null){
                   httpMethods.add("POST");
               }else if (AnnotationUtils.getAnnotation(method, PutMapping.class) != null){
                   httpMethods.add("PUT");
               }else if (AnnotationUtils.getAnnotation(method, DeleteMapping.class) != null){
                   httpMethods.add("DELETE");
               }else if (AnnotationUtils.getAnnotation(method, RequestMapping.class) != null){
                   httpMethods.addAll(Col.as("GET", "POST", "PUT", "DELETE"));
               }
                return httpMethods;
           }
       };
   }

    default void addRequestParamHandlers(Collection<RequestParamHandler> requestParamHandlers){

    }

   default void addRequestExampleReader(Collection<RequestExampleReader> requestExampleReaders){

   }

   default void addRequestHeadersHandler(Collection<RequestHeadersHandler> requestHeadersHandlers){

   }

   default void addResponseExampleReader(Collection<ResponseExampleReader> responseExampleReaders){

   }
}

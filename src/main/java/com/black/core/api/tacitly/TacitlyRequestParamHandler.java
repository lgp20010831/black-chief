package com.black.core.api.tacitly;

import com.black.api.ApiRemark;
import com.black.core.aop.StartPage;
import com.black.core.api.ApiWrapperFactory;
import com.black.core.api.handler.RequestParamHandler;
import com.black.core.api.pojo.ApiParameterDetails;
import com.black.core.json.Ignore;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TacitlyRequestParamHandler implements RequestParamHandler {

    @Override
    public Class<? extends Annotation> filterUselessParamsAnnoation(Class<?> pojoClass) {
        return Ignore.class;
    }

    @Override
    public boolean filterRemainingParam(Class<?> fieldType, Field field, String fieldName) {
        return  !fieldType.equals(Date.class) &&
                !fieldType.equals(Timestamp.class) && !"is_deleted".equals(fieldName);
    }

    @Override
    public void postWrapper(Class<?> controllerClass, Field field, ApiParameterDetails initDetails) {
        ApiRemark remark = AnnotationUtils.getAnnotation(field, ApiRemark.class);
        if (remark != null){
            initDetails.setRemark(remark.value());
        }
        if (initDetails.getRemark() == null){
            initDetails.setRemark(field.getName());
        }
    }

    @Override
    public void filterMethodParams(Class<?> controllerClass, Method method, List<ApiParameterDetails> details,
                                   List<Class<?>> dependPojoClass, ApiWrapperFactory wrapperFactory) {
        String methodName = method.getName();
        List<ApiParameterDetails> newDetails = new ArrayList<>();
        if (AnnotationUtils.getAnnotation(method, StartPage.class) != null){
            newDetails.add(wrapperFactory.createParamDetails("pageNum", "int", "当前页数", false));
            newDetails.add(wrapperFactory.createParamDetails("pageSize", "int", "每页数据量", false));
        }

        Parameter[] parameters = method.getParameters();
        boolean save = false;
        for (Parameter parameter : parameters) {
            RequestParam requestParam;
            RequestPart requestPart;
            if ((requestParam = AnnotationUtils.getAnnotation(parameter, RequestParam.class)) != null){
                Class<?> type = parameter.getType();
                if (!dependPojoClass.contains(type)){
                    String value = requestParam.value();
                    String name = (value == null || "".equals(value)) ? parameter.getName() : value;
                    newDetails.add(wrapperFactory.createParamDetails(name, type.getSimpleName(), name, requestParam.required()));
                }else {
                    save = true;
                }
            }else if (AnnotationUtils.getAnnotation(parameter, RequestBody.class) != null){
                save = true;
            }else if ((requestPart = AnnotationUtils.getAnnotation(parameter, RequestPart.class)) != null){
                String value = requestPart.value();
                String name = (value == null || "".equals(value)) ? parameter.getName(): value;
                Class<?> type = parameter.getType();
                newDetails.add(wrapperFactory.createParamDetails(name, type.getSimpleName(), name, requestPart.required()));
            }
        }

        if (!save){
            details.clear();
        }
        details.addAll(newDetails);
        if (methodName.startsWith("query") || methodName.startsWith("select") || methodName.startsWith("get")){
            for (ApiParameterDetails detail : details) {
                if ("id".equals(detail.getName())){
                    detail.setRequired(true);
                }
            }
        }else if (methodName.startsWith("insert")){
            details.removeIf(d -> "id".equals(d.getName()));
        }else if (methodName.startsWith("delete")){
            details.removeIf(d -> !"id".equals(d.getName()));
        }
    }
}

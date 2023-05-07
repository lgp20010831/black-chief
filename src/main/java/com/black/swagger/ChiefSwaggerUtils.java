package com.black.swagger;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.WebMvcRequestHandler;

public class ChiefSwaggerUtils {

    public static Object getBean(HandlerMethod handlerMethod, BeanFactory beanFactory){
        Object bean = handlerMethod.getBean();
        if (bean instanceof String){
            return beanFactory.getBean(bean.toString());
        }else {
            return bean;
        }
    }

    public static HandlerMethod findControllerType(OperationContext operationContext){
        ClassWrapper<? extends OperationContext> wrapper = ClassWrapper.get(operationContext.getClass());
        FieldWrapper requestContext = wrapper.getField("requestContext");
        Object requestContextValue = requestContext.getValue(operationContext);
        ClassWrapper<RequestMappingContext> classWrapper = ClassWrapper.get(RequestMappingContext.class);
        FieldWrapper handler = classWrapper.getField("handler");
        Object handlerValue = handler.getValue(requestContextValue);
        ClassWrapper<WebMvcRequestHandler> requestHandlerClassWrapper = ClassWrapper.get(WebMvcRequestHandler.class);
        FieldWrapper handlerMethod = requestHandlerClassWrapper.getField("handlerMethod");
        return (HandlerMethod) handlerMethod.getValue(handlerValue);
    }

    public static HandlerMethod findControllerTypeByRequestMappingContext(RequestMappingContext requestMappingContext){
        ClassWrapper<? extends RequestMappingContext> wrapper = ClassWrapper.get(requestMappingContext.getClass());
        FieldWrapper handler = wrapper.getField("handler");
        Object handlerValue = handler.getValue(requestMappingContext);
        ClassWrapper<WebMvcRequestHandler> requestHandlerClassWrapper = ClassWrapper.get(WebMvcRequestHandler.class);
        FieldWrapper handlerMethod = requestHandlerClassWrapper.getField("handlerMethod");
        return (HandlerMethod) handlerMethod.getValue(handlerValue);
    }


    public static String findApiModel(Class<?> entityClass){
        ApiModel annotation = AnnotationUtils.findAnnotation(entityClass, ApiModel.class);
        if (annotation == null){
            return entityClass.getSimpleName();
        }else {
            String value = annotation.value();
            return StringUtils.hasText(value) ? value : entityClass.getSimpleName();
        }
    }


}

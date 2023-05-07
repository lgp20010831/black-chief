package com.black.swagger.v2;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.query.ClassWrapper;
import com.black.core.util.Av0;
import com.black.core.util.LazyAutoWried;
import com.black.swagger.ChiefSwaggerUtils;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Method;
import java.util.Set;

import static com.black.swagger.ChiefIbatisAdaptivePlugin.dofindControllerType;

public class ChiefOpenResponseCommonTypePlugin implements OperationBuilderPlugin, BeanFactoryAware {
    private BeanFactory beanFactory;

    @LazyAutoWried
    TypeResolver typeResolver;


    @Override
    public void apply(OperationContext operationContext) {
        ClassWrapper<?> classWrapper = dofindControllerType(operationContext, beanFactory);
        Class<?> clazz = classWrapper.get();
        GlobalEnhanceRestController annotation = AnnotationUtils.getAnnotation(clazz, GlobalEnhanceRestController.class);
        if (annotation != null){
            HandlerMethod handlerMethod = ChiefSwaggerUtils.findControllerType(operationContext);
            Method method = handlerMethod.getMethod();
            Class<?> returnType = method.getReturnType();
            if (void.class.equals(returnType)){
                Class<? extends RestResponse> responseClassType = AopControllerIntercept.getResponseClassType(clazz);
                operationContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(responseClassType));
                Set<ResponseMessage> messages = Av0.set(new ResponseMessageBuilder()
                        .code(200)
                        .message("OK")
                        .responseModel(new ModelRef(ChiefSwaggerUtils.findApiModel(responseClassType))).build());
                operationContext.operationBuilder().responseMessages(messages);
            }
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}

package com.black.swagger;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.LazyAutoWried;
import com.black.core.util.StringUtils;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

public class ChiefResponseModelPlugin implements OperationBuilderPlugin , BeanFactoryAware {

    @LazyAutoWried
    TypeResolver typeResolver;

    BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void apply(OperationContext operationContext) {
        ChiefSwaggerResponseAdaptive annotation = operationContext.findAnnotation(ChiefSwaggerResponseAdaptive.class).orNull();
        if (annotation != null){
            Class<?> entityClass = findClass(annotation, operationContext);
            operationContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(entityClass));
            operationContext.operationBuilder().responseModel(new ModelRef(ChiefSwaggerUtils.findApiModel(entityClass)));
        }
    }

    public Class<?> findClass(ChiefSwaggerResponseAdaptive annotation, OperationContext operationContext){
        String value = annotation.value();
        Class<?> target = annotation.target();
        if (StringUtils.hasText(value)){
            HandlerMethod handlerMethod = ChiefSwaggerUtils.findControllerType(operationContext);
            Object bean = ChiefSwaggerUtils.getBean(handlerMethod, beanFactory);
            ClassWrapper<?> classWrapper = ClassWrapper.get(handlerMethod.getBeanType());
            MethodWrapper methodWrapper = classWrapper.getSingleMethod(value);
            return (Class<?>) methodWrapper.invoke(bean);
        }else if (!target.equals(void.class)){
            return target;
        }
        throw new IllegalStateException("ill format of annotation");
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}

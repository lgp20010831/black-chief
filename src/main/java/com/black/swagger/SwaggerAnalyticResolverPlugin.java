package com.black.swagger;

import com.black.core.query.ClassWrapper;
import com.black.core.util.LazyAutoWried;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import static com.black.swagger.ChiefSwaggerUtils.findApiModel;

@Order
public class SwaggerAnalyticResolverPlugin implements ParameterBuilderPlugin, BeanFactoryAware {

    private BeanFactory beanFactory;

    @LazyAutoWried
    TypeResolver typeResolver;

    private final RequestResolver requestResolver;

    public SwaggerAnalyticResolverPlugin() {
        requestResolver = new RequestResolver();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void apply(ParameterContext parameterContext) {
        ResolvedMethodParameter methodParameter = parameterContext.resolvedMethodParameter();
        SwaggerAnalytic annotation = methodParameter.findAnnotation(SwaggerAnalytic.class).orNull();
        if (annotation != null){
            String request = annotation.request();
            Class<?> javaClass = parseRequest(request);
            ClassWrapper<?> classWrapper = ClassWrapper.get(javaClass);
            parameterContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(javaClass));
            ParameterBuilder builder = parameterContext.parameterBuilder();
            builder.parameterType("body").modelRef(new ModelRef(findApiModel(javaClass)));
        }
    }

    private Class<?> parseRequest(String request){
        return requestResolver.parseRequest(request);
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}

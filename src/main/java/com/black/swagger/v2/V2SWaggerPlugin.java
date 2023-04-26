package com.black.swagger.v2;

import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Av0;
import com.black.core.util.LazyAutoWried;
import com.black.swagger.ChiefSwaggerUtils;
import com.fasterxml.classmate.TypeResolver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import static com.black.swagger.ChiefIbatisAdaptivePlugin.dofindControllerType;
import static com.black.swagger.ChiefSwaggerUtils.findApiModel;


@Log4j2 @SuppressWarnings("all")
public class V2SWaggerPlugin implements ParameterBuilderPlugin, BeanFactoryAware, OperationBuilderPlugin {


    private BeanFactory beanFactory;

    @LazyAutoWried
    TypeResolver typeResolver;

    private DataSource dataSource;

    private final ApiInvokeMethodRequestResolver requestResolver;

    public V2SWaggerPlugin() {
        requestResolver = new ApiInvokeMethodRequestResolver();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void init(ClassWrapper<?> controllerType){
        if (dataSource == null){
            OptDataSource annotation = controllerType.getAnnotation(OptDataSource.class);
            Class<? extends DataSourceBuilder> type = annotation == null ? SpringDataSourceBuilder.class : annotation.value();
            DataSourceBuilder builder = DataSourceBuilderTypeManager.getBuilder(type);
            dataSource = builder.getDataSource();
        }
        try {
            Connection connection = dataSource.getConnection();
            requestResolver.setConnection(connection);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    private void end(){
        Connection connection = requestResolver.getConnection();
        if (connection != null){
            SQLUtils.closeConnection(connection);
        }
    }

    @Override
    public void apply(OperationContext operationContext) {
        ClassWrapper<?> classWrapper = dofindControllerType(operationContext, beanFactory);
        init(classWrapper);
        try {
            Class<?> mapperClass = getJavaClass(operationContext, classWrapper);
            if (mapperClass != null){
                operationContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(mapperClass));
                Set<ResponseMessage> messages = Av0.set(new ResponseMessageBuilder()
                        .code(200)
                        .message("OK")
                        .responseModel(new ModelRef(ChiefSwaggerUtils.findApiModel(mapperClass))).build());
                operationContext.operationBuilder().responseMessages(messages);
            }
        }catch (Throwable ex){
            CentralizedExceptionHandling.handlerException(ex);
        }finally {
            end();
        }
    }

    @Override
    public void apply(ParameterContext parameterContext) {
        ResolvedMethodParameter methodParameter = parameterContext.resolvedMethodParameter();
        V2Swagger annotation = methodParameter.findAnnotation(V2Swagger.class).orNull();
        if (annotation != null){
            ClassWrapper<?> controllerType = dofindControllerType(parameterContext.getOperationContext(), beanFactory);
            init(controllerType);
            try {
                String request = annotation.value();
                Class<?> javaClass = requestResolver.parseRequest(request, controllerType.getPrimordialClass());
                ClassWrapper<?> classWrapper = ClassWrapper.get(javaClass);
                parameterContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(javaClass));
                ParameterBuilder builder = parameterContext.parameterBuilder();
                builder.parameterType("body").modelRef(new ModelRef(findApiModel(javaClass)));
            }catch (Throwable ex){
                CentralizedExceptionHandling.handlerException(ex);
            }finally {
                end();
            }
        }
    }

    public Class<?> getJavaClass(OperationContext operationContext, ClassWrapper<?> classWrapper ){
        Class<?> mapperClass = null;
        V2Swagger optional = operationContext.findAnnotation(V2Swagger.class).orNull();
        if (optional == null) {
            optional = classWrapper.getAnnotation(V2Swagger.class);
        }

        if (optional == null){
            return null;
        }
        mapperClass = requestResolver.parseRequest(optional.value(), classWrapper.getPrimordialClass());
        return mapperClass;
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }


}

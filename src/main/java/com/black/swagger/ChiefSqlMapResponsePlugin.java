package com.black.swagger;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.action.AutoMapperController;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.Av0;
import com.black.core.util.LazyAutoWried;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.fasterxml.classmate.TypeResolver;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import static com.black.swagger.ChiefIbatisAdaptivePlugin.*;
import static com.black.swagger.ChiefSwaggerUtils.findControllerType;

@Log4j2
public class ChiefSqlMapResponsePlugin implements OperationBuilderPlugin, BeanFactoryAware {

    @LazyAutoWried
    TypeResolver typeResolver;

    BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void apply(OperationContext operationContext) {
        if (!operationContext.getReturnType().isInstanceOf(Object.class)) {
            return;
        }

        ChiefMapSqlAdaptive annotation = operationContext.findAnnotation(ChiefMapSqlAdaptive.class).orNull();
        if (annotation == null){
            return;
        }
        try {
            String tableName;
            tableName = annotation.value();
            if (!StringUtils.hasText(tableName)){
                String tableNameMethodName = annotation.mappingTableNameMethodName();
                HandlerMethod handlerMethod = findControllerType(operationContext);
                Object bean = beanFactory.getBean(handlerMethod.getBean().toString());
                Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
                ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
                MethodWrapper methodWrapper = classWrapper.getSingleMethod(tableNameMethodName);
                Assert.notNull(methodWrapper, "unknown mapping table name method: " + tableNameMethodName);
                tableName = (String) methodWrapper.invoke(bean);
            }
            mapSqlProcessor(tableName, operationContext);
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
            log.info("处理控制器方法映射失败:{}", operationContext.httpMethod().name());
        }

    }

    public void mapSqlProcessor(String tableName, OperationContext operationContext){
        Class<?> mapperClass = getMapperClass(operationContext);
        DataSource dataSource = obtainDataSource(mapperClass);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
            Class<?> ctClass = JDBCTableCtClassManager.getCtClass(tableMetadata);
            operationContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(ctClass));
            Set<ResponseMessage> messages = Av0.set(new ResponseMessageBuilder()
                    .code(200)
                    .message("OK")
                    .responseModel(new ModelRef(ChiefSwaggerUtils.findApiModel(ctClass))).build());
            operationContext.operationBuilder().responseMessages(messages);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
    }

    public Class<?> getMapperClass(OperationContext operationContext){
        Class<?> mapperClass = null;
        SwaggerGlobalMapperAdaptive optional = operationContext.findAnnotation(SwaggerGlobalMapperAdaptive.class).orNull();
        if (optional == null) {
            ClassWrapper<?> classWrapper = dofindControllerType(operationContext, beanFactory);
            optional = classWrapper.getAnnotation(SwaggerGlobalMapperAdaptive.class);
            Class<?> raw = classWrapper.get();
            if (optional == null && AutoMapperController.class.isAssignableFrom(raw)){
                mapperClass = AutoMapperController.doFindType0(raw);
            }
        }

        if (optional != null){
            mapperClass = optional.value();
        }

        Assert.notNull(mapperClass, "unknown mapper type");
        return checkMapperClass(mapperClass);
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}

package com.black.swagger;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.action.AutoMapperController;
import com.black.core.sql.annotation.GlobalConfiguration;
import com.black.core.sql.annotation.ImportMapper;
import com.black.core.sql.annotation.ImportMapperAndPlatform;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.datasource.DataSourceBuilderManager;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.LazyAutoWried;
import com.black.core.util.StringUtils;
import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.fasterxml.classmate.TypeResolver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.black.swagger.ChiefSwaggerUtils.findApiModel;
import static com.black.swagger.ChiefSwaggerUtils.findControllerType;

@Order
@Log4j2
public class ChiefIbatisAdaptivePlugin implements ParameterBuilderPlugin, BeanFactoryAware {

    @LazyAutoWried
    TypeResolver typeResolver;

    BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void apply(ParameterContext parameterContext) {
        ResolvedMethodParameter methodParameter = parameterContext.resolvedMethodParameter();
        ChiefIbatisAdaptive annotation = methodParameter.findAnnotation(ChiefIbatisAdaptive.class).orNull();
        if (annotation != null){
            ParameterBuilder builder = parameterContext.parameterBuilder();
            Class<?> entityClass = findClass(annotation, parameterContext);
            parameterContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(entityClass));
            builder.parameterType("body")
                    .modelRef(new ModelRef(findApiModel(entityClass)));
        }
        ChiefMapSqlAdaptive adaptive = methodParameter.findAnnotation(ChiefMapSqlAdaptive.class).orNull();
        if (adaptive != null){
            try {
                String tableName;
                tableName = adaptive.value();
                if (!StringUtils.hasText(tableName)){
                    String tableNameMethodName = adaptive.mappingTableNameMethodName();
                    HandlerMethod handlerMethod = findControllerType(parameterContext.getOperationContext());
                    Object bean = ChiefSwaggerUtils.getBean(handlerMethod, beanFactory);
                    Class<?> primordialClass = handlerMethod.getBeanType();
                    ClassWrapper<?> classWrapper = ClassWrapper.get(primordialClass);
                    MethodWrapper methodWrapper = classWrapper.getSingleMethod(tableNameMethodName);
                    Assert.notNull(methodWrapper, "unknown mapping table name method: " + tableNameMethodName);
                    tableName = (String) methodWrapper.invoke(bean);
                }
                mapSqlProcessor(tableName, parameterContext, methodParameter);
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                log.info("处理控制器方法映射失败:{}", methodParameter.defaultName());
            }
        }
    }


    public void mapSqlProcessor(String tableName, ParameterContext parameterContext, ResolvedMethodParameter methodParameter){
        Class<?> mapperClass = getMapperClass(parameterContext, methodParameter);
        DataSource dataSource = obtainDataSource(mapperClass);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
            Class<?> ctClass = JDBCTableCtClassManager.getCtClass(tableMetadata);
            parameterContext.getDocumentationContext().getAdditionalModels().add(typeResolver.resolve(ctClass));
            ParameterBuilder builder = parameterContext.parameterBuilder();
            builder.parameterType("body")
                    .modelRef(new ModelRef(findApiModel(ctClass)));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
    }

    public static DataSource obtainDataSource(Class<?> mapperClass){
        ClassWrapper<?> cw = ClassWrapper.get(mapperClass);
        GlobalConfiguration configuration = cw.getAnnotation(GlobalConfiguration.class);
        String alias = configuration.value();
        Class<? extends DataSourceBuilder> builderClass = configuration.builderClass();
        DataSourceBuilder dataSourceBuilder = DataSourceBuilderManager.obtain(alias, () -> {
            return DataSourceBuilderTypeManager.getBuilder(builderClass);
        });
        if (dataSourceBuilder instanceof AliasAware){
            ((AliasAware) dataSourceBuilder).setAlias(alias);
        }
        return dataSourceBuilder.getDataSource();
    }

    public Class<?> getMapperClass(ParameterContext parameterContext, ResolvedMethodParameter methodParameter){
        Class<?> mapperClass = null;
        SwaggerGlobalMapperAdaptive optional = methodParameter.findAnnotation(SwaggerGlobalMapperAdaptive.class).orNull();
        if (optional == null) {
            ClassWrapper<?> classWrapper = dofindControllerType(parameterContext.getOperationContext(), beanFactory);
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
        mapperClass = checkMapperClass(mapperClass);
        return mapperClass;
    }

    public static Class<?> checkMapperClass(Class<?> mapperClass){
        ClassWrapper<?> cw = ClassWrapper.get(mapperClass);
        for (;;){
            if (cw.hasAnnotation(GlobalConfiguration.class)){
                return mapperClass;
            }

            if(cw.hasAnnotation(ImportMapper.class)){
                Class<?> value = cw.getAnnotation(ImportMapper.class).value();
                if (mapperClass.equals(value)){
                    throw new IllegalStateException("依赖mapper循环: " + value);
                }
                cw = ClassWrapper.get(value);
                continue;
            }

            if (cw.hasAnnotation(ImportMapperAndPlatform.class)){
                Class<?> value = cw.getAnnotation(ImportMapperAndPlatform.class).value();
                if (mapperClass.equals(value)){
                    throw new IllegalStateException("依赖mapper循环: " + value);
                }
                cw = ClassWrapper.get(value);
                continue;
            }
            throw new IllegalStateException("ill mapper: " + mapperClass);
        }
    }

    public Class<?> findClass(ChiefIbatisAdaptive annotation, ParameterContext parameterContext){
        String value = annotation.value();
        Class<?> target = annotation.target();
        if (StringUtils.hasText(value)){
            HandlerMethod handlerMethod = findControllerType(parameterContext.getOperationContext());
            Assert.notNull(handlerMethod, "not find handler method");
            Object bean = ChiefSwaggerUtils.getBean(handlerMethod, beanFactory);
            ClassWrapper<?> classWrapper = ClassWrapper.get(handlerMethod.getBeanType());
            MethodWrapper methodWrapper = classWrapper.getSingleMethod(value);
            return (Class<?>) methodWrapper.invoke(bean);
        }else if (!target.equals(void.class)){
            return target;
        }
        throw new IllegalStateException("ill format of annotation");
    }

    public static ClassWrapper<?> dofindControllerType(OperationContext operationContext, BeanFactory beanFactory){
        HandlerMethod handlerMethod = findControllerType(operationContext);
        Assert.notNull(handlerMethod, "not find handler method");
        return ClassWrapper.get(handlerMethod.getBeanType());
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }


}

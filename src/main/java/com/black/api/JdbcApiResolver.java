package com.black.api;

import com.black.holder.SpringHodler;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Body;
import com.black.syntax.SyntaxResolverManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;

import static com.black.api.ApiV2Utils.*;

@SuppressWarnings("all")
public class JdbcApiResolver extends AbstractApiParser{

    @Override
    public boolean support(MethodWrapper mw) {
        ApiJdbcProperty annotation = AnnotationUtils.findAnnotation(mw.get(), ApiJdbcProperty.class);
        return annotation != null && !annotation.hide();
    }

    protected ApiJdbcPropertyConfig getConfig(MethodWrapper mw){
        ApiJdbcProperty annotation =  AnnotationUtils.findAnnotation(mw.get(), ApiJdbcProperty.class);
        Assert.notNull(annotation, "ApiJdbcProperty annotation is null");
        return AnnotationUtils.loadAttribute(annotation, new ApiJdbcPropertyConfig());
    }

    @Override
    public void doResolve(MethodWrapper mw, HttpMethod method, Configuration configuration, Class<?> type) {
        ClassWrapper<?> controllerWrapper = ClassWrapper.get(type);
        Connection connection = configuration.getConnection();
        Connection protogenesisConnection = connection;
        Connection dynamicConnection = getDynamicConnection(mw, controllerWrapper, configuration);
        boolean isDynamic = false;
        if (dynamicConnection != null){
            isDynamic = true;
            connection = dynamicConnection;
        }
        try {
            Assert.notNull(connection, "读取jdbc 连接不能为空");
            AliasColumnConvertHandler handler = configuration.getConvertHandler();

            ApiJdbcPropertyConfig config = getConfig(mw);
            String responseFormat = config.getResponse();
            String requestFormat = config.getRequest();
            Body body = new Body();
            body.put(HTTP_METHOD_NAME, method)
                    .put(RESPONSE_CLASS_NAME, configuration.getResponseClass(type))
                    .put(CONNECTION_NAME, connection)
                    .put(ALIAS_COLUMN_NAME, handler)
                    .put(CONTROLLER_TYPE_NAME, type)
                    .put(METHOD_WRAPPER_NAME, mw);


            SyntaxResolverManager responseManager = SyntaxResolverManager.instance("response-api", syntaxResolverManager -> {
                syntaxResolverManager.setInstanceDefaultResolver(new ResponseBlendSyntaxResolver());
                syntaxResolverManager.registerPrivateResolver(new ResponseDefaultParseJsonResolver());
                syntaxResolverManager.registerPrivateResolver(new ApiResponseBlentStrengthenResolver());
            });
            responseFormat = RESPONSE_PREFIX + responseFormat;
            //解析响应
            responseManager.resolve0(responseFormat, body, null);

            SyntaxResolverManager requestManager = SyntaxResolverManager.instance("request-api", syntaxResolverManager -> {
                syntaxResolverManager.setInstanceDefaultResolver(new BlendJdbcSyntaxResolver());
                syntaxResolverManager.registerPrivateResolver(new ApiUrlSyntaxResolver());
                syntaxResolverManager.registerPrivateResolver(new ApiDefaultParseJsonSyntaxResolver());
                syntaxResolverManager.registerPrivateResolver(new ApiRequestBlentStrengthenResolver());
                syntaxResolverManager.registerPrivateInterlocutor(new SpringRequestParamAutoFindLocutor());
            });

            //解析请求
            requestManager.resolve0(requestFormat, body, null);

            String[] headers = config.getHttpHeaders();
            handleHeaders(controllerWrapper, method, headers, configuration, mw);
            setRemarkOfMethod(mw, method, config.getRemark());
        }finally {
            if (isDynamic) {
                closeDynamicConnection(connection, mw, controllerWrapper);
                connection = protogenesisConnection;
            }
        }
    }



    private void closeDynamicConnection(Connection connection, MethodWrapper mw, ClassWrapper<?> cw){
        DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
        ApiDynamicDataSource annotation = getDynamicAnnotation(mw, cw);
        if (annotation != null){
            ApiDynamicType type = annotation.type();
            String name = annotation.name();
            if (type == ApiDynamicType.CHIEF){
                ConnectionManagement.closeCurrentConnection(name);
            }else {
                MybatisPlusDynamicCut.poll();
                DataSource dataSource = beanFactory.getBean(DataSource.class);
                MybatisPlusDynamicCut.closeConnection(connection, dataSource);
            }
        }
    }

    private Connection getDynamicConnection(MethodWrapper mw, ClassWrapper<?> cw, Configuration configuration){
        DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
        ApiDynamicDataSource annotation = getDynamicAnnotation(mw, cw);
        if (annotation == null){
            return null;
        }
        ApiDynamicType dynamicType = annotation.type();
        String name = annotation.name();
        if (dynamicType == ApiDynamicType.CHIEF){
            return ConnectionManagement.getConnection(name);
        }else if (dynamicType == ApiDynamicType.MYBATIS_PLUS){
            MybatisPlusDynamicCut.push(name);
            DataSource dataSource = beanFactory.getBean(DataSource.class);
            return DataSourceUtils.getConnection(dataSource);
        }
        return null;
    }

    private ApiDynamicDataSource getDynamicAnnotation(MethodWrapper mw, ClassWrapper<?> cw){
        ApiDynamicDataSource annotation = mw.getAnnotation(ApiDynamicDataSource.class);
        if (annotation == null){
            annotation = cw.getAnnotation(ApiDynamicDataSource.class);
        }
        if (annotation == null){
            return null;
        }
        return annotation;
    }


}

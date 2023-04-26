package com.black.api.swagger;

import com.black.api.ApiJdbcPropertyConfig;
import com.black.api.JdbcApiResolver;
import com.black.api.ResponseData;
import com.black.api.handler.TrustBeanMetadataResolver;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.swagger.v2.V2Swagger;
import com.black.utils.ReflectionUtils;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SwaggerJdbcPropertyApiResolver extends JdbcApiResolver {


    public SwaggerJdbcPropertyApiResolver(){
        TrustBeanMetadataResolver.obtainRemarkQueue.add(fw -> {
            ApiModelProperty annotation = fw.getAnnotation(ApiModelProperty.class);
            return annotation == null ? null : annotation.value();
        });
    }

    @Override
    public boolean support(MethodWrapper mw) {
        ApiOperation annotation = mw.getAnnotation(ApiOperation.class);
        return annotation != null && !annotation.hidden();
    }

    @Override
    protected ApiJdbcPropertyConfig getConfig(MethodWrapper mw) {
        ApiOperation annotation = AnnotationUtils.findAnnotation(mw.get(), ApiOperation.class);
        ApiJdbcPropertyConfig config = new ApiJdbcPropertyConfig();
        config.setHide(annotation.hidden());
        config.setRemark(annotation.value());
        HashSet<String> headers = new HashSet<>();
        headers.add("Content-Type:application/json");
        List<ParameterWrapper> pws = mw.getParameterByAnnotation(RequestHeader.class);
        for (ParameterWrapper pw : pws) {
            RequestHeader requestHeader = pw.getAnnotation(RequestHeader.class);
            String key = StringUtils.hasText(requestHeader.value()) ? requestHeader.value() : pw.getName();
            headers.add(key + ":example");
        }
        config.setHttpHeaders(headers.toArray(new String[0]));
        Class<?> returnType = mw.getReturnType();
        String response = castReturnTypeToBlend(returnType, mw);
        config.setResponse(response);
        ParameterWrapper pw = mw.getSingleParameterByAnnotation(RequestBody.class);
        String request = "";
        if (pw != null){
            request = castParamTypeToBlend(pw.getType(), pw);
        }
        config.setRequest(request);
        return config;
    }


    protected String castParamTypeToBlend(Class<?> type, ParameterWrapper pw){
        V2Swagger swagger = pw.getAnnotation(V2Swagger.class);
        if(swagger != null){
            return "$S: " + swagger.value();
        }
        SwaggerCastToApiConfiguration configuration = SwaggerCastToApiConfiguration.getInstance();
        AliasColumnConvertHandler convertHandler = configuration.getColumnConvertHandler();
        if (Map.class.isAssignableFrom(type)){
            return "$R: {}";
        }else if (Collection.class.isAssignableFrom(type)){
            Class<?>[] methodReturnGenericVals = ReflectionUtils.getMethodParamterGenericVals(pw.get());
            if (methodReturnGenericVals.length != 1) {
                return "$R: []";
            }
            Class<?> genericVal = methodReturnGenericVals[0];
            String tableName = convertHandler.convertColumn(StringUtils.titleLower(genericVal.getSimpleName()));
            return tableName + "[]";
        }
        String tableName = convertHandler.convertColumn(StringUtils.titleLower(type.getSimpleName()));
        return tableName + "{}";
    }
    protected String castReturnTypeToBlend(Class<?> type, MethodWrapper mw){
        if (void.class.equals(type)){
            return "";
        }
        V2Swagger swagger = mw.getAnnotation(V2Swagger.class);
        if (swagger != null){
            return "$S: " + swagger.value();
        }
        SwaggerCastToApiConfiguration configuration = SwaggerCastToApiConfiguration.getInstance();
        AliasColumnConvertHandler convertHandler = configuration.getColumnConvertHandler();
        if (Map.class.isAssignableFrom(type)){
            return "$R: {}";
        }else if (Collection.class.isAssignableFrom(type)){
            Class<?>[] methodReturnGenericVals = ReflectionUtils.getMethodReturnGenericVals(mw.get());
            if (methodReturnGenericVals.length != 1) {
                return "$R: []";
            }
            Class<?> genericVal = methodReturnGenericVals[0];
            String tableName = convertHandler.convertColumn(StringUtils.titleLower(genericVal.getSimpleName()));
            return tableName + "[]";
        }

        Class<?> dataType = type;
        if (RestResponse.class.isAssignableFrom(type)){
            ClassWrapper<?> cw = ClassWrapper.get(type);
            FieldWrapper fw = cw.getSingleFieldByAnnotation(ResponseData.class);
            if (fw == null){
                return "";
            }

            //如果是泛型
            if (fw.isGenericType()) {
                Class<?>[] methodReturnGenericVals = ReflectionUtils.getMethodReturnGenericVals(mw.get());
                if (methodReturnGenericVals.length != 1) {
                    return "";
                }
                dataType = methodReturnGenericVals[0];
                if (Collection.class.isAssignableFrom(dataType)){
                    //得再次取泛型, 解析不了
                    return "";
                }
            }else {
                dataType = fw.getType();
            }
        }

        String tableName = convertHandler.convertColumn(StringUtils.titleLower(dataType.getSimpleName()));
        return tableName + "{}";
    }
}

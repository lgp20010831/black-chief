package com.black.api;

import com.alibaba.fastjson.JSONObject;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.SetGetUtils;
import com.black.syntax.AbstractSyntaxResolver;
import com.black.syntax.SyntaxMetadataListener;
import org.springframework.util.Assert;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static com.black.api.ApiV2Utils.*;

public abstract class AbstractApiResponseSyntaxResolver extends AbstractSyntaxResolver {


    public AbstractApiResponseSyntaxResolver(String flag) {
        super(RESPONSE_PREFIX + flag);
    }

    @Override
    public Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener) {
        Object obj = source.get(HTTP_METHOD_NAME);
        if (obj ==  null){
            throw new IllegalStateException("not set http method");
        }
        ClassWrapper<?> responseClass = (ClassWrapper<?>) source.get(RESPONSE_CLASS_NAME);
        HttpMethod httpMethod = (HttpMethod) obj;
        Object data = resolveHttpMethod(expression, httpMethod, source);
        JSONObject object = new JSONObject();
        String dataKey = null;
        for (FieldWrapper fw : responseClass.getFields()) {
            if (SetGetUtils.hasGetMethod(fw.getField())){
                if (fw.hasAnnotation(ResponseData.class)){
                    dataKey = fw.getName();
                    object.put(dataKey, "null");
                    continue;
                }
                Class<?> fwType = fw.getType();
                String alias = fw.getName();
                if (fwType != null && fwType.equals(Boolean.class)){
                    writeBoolean(object, alias);
                }else
                if (fwType != null && Number.class.isAssignableFrom(fwType)){
                    wriedInt(object, alias);
                }else if ((Date.class.equals(fwType) || Time.class.equals(fwType) || Timestamp.class.equals(fwType) || LocalDateTime.class.equals(fwType))){
                    wriedDate(object, alias);
                }else {
                    wriedString(object, alias, fwType);
                }
            }
        }
        Assert.notNull(dataKey, "响应类需要指定响应结果载体");
        object.put(dataKey, data);
        httpMethod.setResponseDome(JSONTool.formatJson(object.toString()));
        return null;
    }

    public abstract Object resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source);

}

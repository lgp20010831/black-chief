package com.black.xml.crud;

import com.black.xml.servlet.MappingMethodInfo;
import com.black.xml.servlet.MvcGenerator;
import com.black.xml.servlet.ServletGenerateUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-02 14:17
 */
@SuppressWarnings("all")
public interface RequestMethodGenerator {


    void generate(MvcGenerator generator);

    default MappingMethodInfo createInfo(String name,
                                           String url,
                                           RequestMethod requestMethod,
                                           String remark,
                                           String body,
                                           boolean page,
                                           boolean voidReturn,
                                           boolean onV2Swagger,
                                           String v2SwaggerValue,
                                           String... params){
        MappingMethodInfo methodInfo = new MappingMethodInfo();
        Map<String, Object> requestMappingInfos = methodInfo.getRequestMappingInfos();
        requestMappingInfos.put("value", new String[]{url});
        requestMappingInfos.put("method", requestMethod);
        methodInfo.getApiOperationInfos().put("value", remark);
        methodInfo.setMethodName(name);
        methodInfo.setOpenPage(page);
        methodInfo.setOnV2Swagger(onV2Swagger);
        methodInfo.setV2SwaggerValue(v2SwaggerValue);
        methodInfo.setBody(body);
        methodInfo.setVoidReturnType(voidReturn);
        methodInfo.setOpenPage(page);
        for (String param : params) {
            ServletGenerateUtils.parseParam2(param, methodInfo);
        }
        return methodInfo;
    }

}

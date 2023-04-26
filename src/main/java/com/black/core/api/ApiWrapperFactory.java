package com.black.core.api;

import com.black.core.api.pojo.ApiController;
import com.black.core.api.pojo.ApiParameterDetails;
import com.black.core.api.pojo.ApiRestInterface;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ApiWrapperFactory {


    public ApiParameterDetails createParamDetails(Field field){
        String name = field.getName();
        Class<?> type = field.getType();
        ApiParameterDetails details = new ApiParameterDetails();
        details.setName(name);
        details.setType(type.getSimpleName());
        return details;
    }

    public ApiParameterDetails createParamDetails(String name, String type, String remark, boolean required){
        return new ApiParameterDetails(type, name, remark, required);
    }

    public ApiRestInterface createRestInterface(String remark,
                                                List<String> urls,
                                                List<String> httpMethods,
                                                Map<String, String> requestHeaders,
                                                List<ApiParameterDetails> requestListDetails,
                                                String requestExample,
                                                String responseExample){
        return new ApiRestInterface(remark, urls, httpMethods, requestHeaders, requestListDetails, requestExample, responseExample);
    }

    public ApiController createControllerWrapper(String remark, List<ApiRestInterface> interfaces){
        return new ApiController(remark, interfaces);
    }
}

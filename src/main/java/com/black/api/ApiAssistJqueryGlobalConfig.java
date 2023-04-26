package com.black.api;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.black.core.json.JsonUtils;
import com.black.utils.NetUtils;
import lombok.Data;

@Data
public class ApiAssistJqueryGlobalConfig {

    private String address;

    //key:value
    private String globalHeaders;

    public ApiAssistJqueryGlobalConfig(){
        address = NetUtils.getObjectIpAddress();
    }

    public String getJson(){
        return JsonUtils.letJson(this).toString(SerializerFeature.WriteMapNullValue);
    }
}

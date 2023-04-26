package com.black.api.swagger;

import com.black.api.Configuration;
import com.black.core.api.ApiConfiguration;
import com.black.core.api.ApiConfigurationHolder;
import com.black.core.util.StringUtils;
import io.swagger.annotations.Api;

public class HandlerSwaggerAdaptation {


    public void addApiScanner(){
        ApiConfiguration apiConfiguration = ApiConfigurationHolder.getConfiguration();
        apiConfiguration.getSelectedScanAnnotationTypes().add(Api.class);
    }

    public void addApiRemarkFun(){
        Configuration.getControllerRemarkFunList.add(type -> {
            Api annotation = type.getAnnotation(Api.class);
            if (annotation == null) return null;
            String[] tags = annotation.tags();
            if (tags.length == 0) return null;
            String tag = tags[0];
            return StringUtils.hasText(tag) ? tag : null;
        });
    }

}

package com.black.core.aop.servlet;


import com.black.core.mvc.response.Response;
import lombok.Data;

@Data
public class AopChiefServletConfiguration {

    private static AopChiefServletConfiguration configuration;


    public synchronized static AopChiefServletConfiguration getInstance() {
        if (configuration == null){
            configuration = new AopChiefServletConfiguration();
        }
        return configuration;
    }

    private boolean useGlobalResponse = false;

    private Class<? extends RestResponse> globalResponseType = Response.class;

    private boolean useGlobalPrintLog = false;

    private boolean globalPrintLog = true;

    private Class<? extends RestResponse> defaultResponseType = Response.class;
}

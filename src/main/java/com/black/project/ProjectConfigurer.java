package com.black.project;

import com.black.core.api.ApiConfiguration;
import com.black.core.api.ApiConfigurationHolder;
import com.black.core.asyn.AsynConfiguration;
import com.black.core.asyn.AsynConfigurationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

@Configuration  @SuppressWarnings("all")
public class ProjectConfigurer implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        configApi();

        configAsyn();
    }

    //配置 chief api 文档环境
    private void configApi(){
        ApiConfiguration apiConfiguration = ApiConfigurationHolder.getConfiguration();
    }

    //配置 chief 全局线程池参数
    private void configAsyn(){
        AsynConfiguration configuration = AsynConfigurationManager.getConfiguration();
    }


}

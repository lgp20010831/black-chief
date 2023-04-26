package com.black.core.factory.beans.dome;

import com.black.aviator.Demo;
import com.black.core.factory.beans.AgentRequired;
import com.black.core.factory.beans.Properties;
import com.black.core.factory.beans.PrototypeBean;
import com.black.core.factory.beans.agent.ProxyType;
import com.black.core.factory.beans.annotation.CompleteMethod;
import com.black.core.yml.YmlConfigurationProperties;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Map;

@AgentRequired(proxyType = ProxyType.INITIALIZATION)
public class Action {

    @PrototypeBean(createBatch = 10)
    List<HikariDataSource> dataSources;

    @CompleteMethod
    @YmlConfigurationProperties
    void complete(@PrototypeBean List<Demo.Po> list,
                  @Properties("mapping") Map<String, String> pros){
        System.out.println(list);
        System.out.println(pros);
        System.out.println(dataSources);
    }


}

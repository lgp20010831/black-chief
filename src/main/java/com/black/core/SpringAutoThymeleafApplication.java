package com.black.core;


import com.black.api.swagger.EnabledCastSwaggerToApi;
import com.black.bin.ApplyProxyFactory;
import com.black.bin.ProxyMetadata;
import com.black.core.annotation.OpenChiefApplication;
import com.black.core.aop.listener.EnableGlobalAopChainWriedModular;
import com.black.core.aviator.annotation.EnabledGlobalAviatorLayer;
import com.black.core.cache.ClassSourceCache;
import com.black.core.data.annotation.EnabledDataTransferStation;
import com.black.core.event.EnableEventAutoDispenser;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.component.BeanFactoryType;
import com.black.core.factory.beans.config_collect520.ResourceCollectionBeanFactory;
import com.black.core.factory.beans.xml.XmlBeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.graphql.ann.EnabledGraphqlTransfer;
import com.black.core.ill.aop.EnabledGlobalThrowableManagement;
import com.black.core.listener.annotation.EnabledApplicationListenerOccur;
import com.black.core.mark.annotation.EnabledGlobalMarkHandler;
import com.black.core.mybatis.plus.EnableAutoAdaptationDynamicllyIbaisPlus;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;
import com.black.core.netty.annotation.EnableNettyAsynAdapter;
import com.black.core.query.ClassWrapper;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.component.DefaultLoadDriver;
import com.black.core.sql.annotation.EnabledMapSQLApplication;
import com.black.core.sql.code.mapping.EnabledGlobalMapping;
import com.black.core.util.IntegratorScanner;
import com.black.mq_v2.annotation.EnabledMqttExt;
import com.black.mvc.MvcMappingRegister;
import com.black.nest.NestDictController;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Map;
import java.util.Set;

@EnableSwagger2
@EnabledCastSwaggerToApi
@EnabledMqttExt
//@EnabledProcessorPolymerizing
//@EnabledHitpClient
//@EnabledHitpServer
@Log4j2

//@EnabledTokenGovern(biko = Biko.class)
//@EnableProxy
//@EnableApiCollector
//@EnableTokenValidator
//@EnableOperationPower
//@EnableWorkflowModule
//@EnableTxtTemplateHolder
//@EnabledAttributeInjection
//@EnabledProcessorPolymerizing
//@EnableIbatisInterceptsDispatcher
//@EnableGlobalTxtUnionApiUnionOperatorCode
//@EnabledManyAopMQClients
//@EnabledMinios
//@EnabledRUPComponent
@EnabledApplicationListenerOccur
@EnabledGraphqlTransfer
@EnabledGlobalMapping
//@EnableHttpActuator
@EnabledGlobalAviatorLayer
@EnabledGlobalMarkHandler
@EnabledMapSQLApplication
@BeanFactoryType(ResourceCollectionBeanFactory.class)
@EnableNettyAsynAdapter
@EnabledDataTransferStation
//@EnabledManyAopMQClients
//@EnableWorkflowRefinedModule
//@EnabledMQttClient("${ldb.mqtt.url}")
//@MapperScan({"com.black.core.yml"})
@SpringBootApplication
//@EnabledSqlPreExecution(position = {"sql/trigger.sql"})
@EnabledGlobalThrowableManagement
@EnableAutoAdaptationDynamicllyIbaisPlus
@EnableGlobalAopChainWriedModular
@OpenChiefApplication
@EnableDynamicallyMultipleClients("com.example.springautothymeleaf.test.contr")
@EnableEventAutoDispenser

//封神辣
//@EntityScan(basePackages = {"com.example.springautothymeleaf.test.jpa.pojo"})
//@EnableJpaRepositories(basePackages = {"com.example.springautothymeleaf.test"})
public class SpringAutoThymeleafApplication {




    public static void main(String[] args) throws Exception {

        ChiefApplicationRunner.openChiefApplication();
        DefaultLoadDriver.openColorLog();
        //SpringRunnerPrint.closePrint();
        //DefaultLoadDriver.closeLog();
        IntegratorScanner.printLossMemory = false;
        ClassSourceCache.printRegisterSize = false;
        ConfigurableApplicationContext context = SpringApplication.run(SpringAutoThymeleafApplication.class, args);
        BeanFactory factory = FactoryManager.getBeanFactory();
        ClassWrapper.clearCache();
        MvcMappingRegister.registerSupportAopController(NestDictController.class);

        Map<Class<?>, ProxyMetadata> proxyMetadataMap = ApplyProxyFactory.getProxyMetadataMap();
        Map<String, Set<Class<?>>> sourceCache = ClassSourceCache.getSourceCache();
        System.out.println(sourceCache.size());
//        System.out.println(proxyMetadataMap);
//
//        DefaultLifecycleProcessor configuration = context.getBean(DefaultLifecycleProcessor.class);
//        configuration.stop();
    }


}

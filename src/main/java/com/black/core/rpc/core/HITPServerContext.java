package com.black.core.rpc.core;

import com.black.bin.InstanceType;
import com.black.nio.code.Configuration;
import com.black.rpc.RpcServer;
import com.black.rpc.RpcWebServerApplicationContext;
import com.black.core.chain.*;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.rpc.annotation.EnabledHitpServer;
import com.black.core.rpc.annotation.HitpAction;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

@Log4j2
@LoadSort(452)
@ChainClient(HITPServerContext.class)
@LazyLoading(EnabledHitpServer.class)
public class HITPServerContext implements OpenComponent, EnabledControlRisePotential, CollectedCilent,
        ChainPremise, ApplicationDriver {

    public static final String HITP_SERVER_PREFIX = "hitp.server";
    public static final String PORT = "port";

    private Collection<Object> actionSource = new HashSet<>();

    private RpcWebServerApplicationContext webServerApplicationContext;

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        EnabledHitpServer annotation = getAnnotation();
        Class<? extends Consumer<Configuration>> hook = annotation.hook();
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        String port = reader.selectAttribute(HITP_SERVER_PREFIX + "." + PORT);
        int p = Integer.parseInt(port);
        try {
            webServerApplicationContext = RpcServer.startServer(p);
            if (!NullHook.class.equals(hook)){
                Consumer<Configuration> consumer = FactoryManager.initAndGetBeanFactory().getSingleBean(hook);
                webServerApplicationContext.setNioConfigurationHook(consumer);
            }
            webServerApplicationContext.bind();
            for (Object action : actionSource) {
                webServerApplicationContext.registerAction(action);
            }
            log.info("hitp 服务器加载完成, 扫描到的 action 接口共: [{}]", webServerApplicationContext.getMethodRegister().methodSize());
        } catch (IOException e) {
            CentralizedExceptionHandling.handlerException(e);
            log.warn("无法启动服务器");
        }
    }

    private EnabledHitpServer getAnnotation(){
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return AnnotationUtils.getAnnotation(mainClass, EnabledHitpServer.class);
    }

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        if (webServerApplicationContext != null){
            try {
                webServerApplicationContext.shutdown();
            }catch (Throwable e){}
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("action", tre -> {
            return BeanUtil.isSolidClass(tre) && tre.isAnnotationPresent(HitpAction.class);
        });
        entry.instance(true);
        entry.setInstanceType(InstanceType.BEAN_FACTORY);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("action".equals(resultBody.getAlias())){
            actionSource.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && AnnotationUtils.getAnnotation(mainClass, EnabledHitpServer.class) != null;
    }
}

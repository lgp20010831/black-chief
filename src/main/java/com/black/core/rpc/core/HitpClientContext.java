package com.black.core.rpc.core;

import com.black.core.chain.*;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.rpc.annotation.EnabledHitpClient;
import com.black.core.rpc.annotation.HitpMapper;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.tools.BeanUtil;
import com.black.holder.SpringHodler;
import com.black.nio.code.Configuration;
import com.black.rpc.RpcClient;
import com.black.rpc.RpcWebClientApplicationContext;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

@Log4j2
@LoadSort(456)
@ChainClient(HitpClientContext.class)
@LazyLoading(EnabledHitpClient.class)
public class HitpClientContext implements OpenComponent, EnabledControlRisePotential, CollectedCilent,
        ChainPremise, ApplicationDriver {

    public static final String HITP_CLIENT_PREFIX = "hitp.client";
    public static final String PORT = "port";
    public static final String HOST = "host";

    private Set<Class<?>> clientMapperClasses = new HashSet<>();
    private RpcWebClientApplicationContext webClientApplicationContext;

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws IOException {
        EnabledHitpClient annotation = getAnnotation();
        Class<? extends Consumer<Configuration>> hook = annotation.hook();
        DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        String port = reader.selectAttribute(HITP_CLIENT_PREFIX + "." + PORT);
        String host = reader.selectAttribute(HITP_CLIENT_PREFIX + "." + HOST);
        int p = Integer.parseInt(port);
        try {

            webClientApplicationContext = RpcClient.startClient(host, p);
            if (!NullHook.class.equals(hook)){
                Consumer<Configuration> consumer = FactoryManager.initAndGetBeanFactory().getSingleBean(hook);
                webClientApplicationContext.setNioConfigurationHook(consumer);
            }
            webClientApplicationContext.connect();
            for (Class<?> clientMapperClass : clientMapperClasses) {
                Object proxyMapper = webClientApplicationContext.proxyMapper(clientMapperClass);
                beanFactory.registerSingleton(NameUtil.getName(proxyMapper), proxyMapper);
            }
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (Object mapper : webClientApplicationContext.getMapperCache()) {
                Class<Object> primordialClass = BeanUtil.getPrimordialClass(mapper);
                joiner.add(primordialClass.getSimpleName());
            }
            log.info("hitp 客户端加载完成, 扫描到的 Mapper: {}", joiner.toString());
        } catch (IOException e) {
            log.warn("加载 hitp 客户端失败");
            throw e;
        }
    }

    private EnabledHitpClient getAnnotation(){
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return AnnotationUtils.getAnnotation(mainClass, EnabledHitpClient.class);
    }

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && AnnotationUtils.getAnnotation(mainClass, EnabledHitpClient.class) != null;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("mapper", tre -> {
            return tre.isInterface() && tre.isAnnotationPresent(HitpMapper.class);
        });
        entry.instance(false);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("mapper".equals(resultBody.getAlias())){
            for (Object obj : resultBody.getCollectSource()) {
                clientMapperClasses.add((Class<?>) obj);
            }
        }
    }
    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        if (webClientApplicationContext != null){
            try {
                webClientApplicationContext.shutdown();
            }catch (Throwable e){}

        }
    }

}

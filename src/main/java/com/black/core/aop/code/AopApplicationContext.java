package com.black.core.aop.code;

import com.black.core.spring.ChiefApplicationRunner;
import com.black.mq_v2.proxy.aop.AopMqttProxyHybrid;
import com.black.core.aop.annotation.ImportHybridRange;
import com.black.core.aop.ibatis.AopIbatisRollBackHybrid;
import com.black.core.aop.servlet.AopEnhanceControllerHybrid;
import com.black.core.aop.servlet.flow.AopFlowIntecept;
import com.black.core.asyn.AsynScheduler;
import com.black.core.aviator.aop.AviatorAopManager;
import com.black.core.cache.ClassSourceCache;
import com.black.core.data.AopDataDriver;
import com.black.core.event.AnnotationEventAutoDispenser;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.ill.aop.IllAopHybrid;
import com.black.core.lock.AopShareLockManager;
import com.black.core.mark.AopMarkResolver;
import com.black.core.util.IntegratorScanner;
import com.black.core.util.SimplePattern;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.code.aop.SQLWriedWrapperHybrid;
import com.black.sql_v2.javassist.aop.SqlV2AopHybrid;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Log4j2
@ImportHybridRange({AopEnhanceControllerHybrid.class, AnnotationEventAutoDispenser.class,
        AopIbatisRollBackHybrid.class, AsynScheduler.class, IllAopHybrid.class,
AopDataDriver.class, AopMarkResolver.class, SQLWriedWrapperHybrid.class, AviatorAopManager.class, AopShareLockManager.class,
        AopFlowIntecept.class, AopMqttProxyHybrid.class, SqlV2AopHybrid.class})
public class AopApplicationContext {

    private static SimplePattern simplePattern;

    private static final Set<Class<?>> source = new HashSet<>();

    private static InstanceFactory instanceFactory;

    private static SpringApplication springApplication;

    public static boolean printScanRange = false;

    public static void setPrintScanRange(boolean printScanRange) {
        AopApplicationContext.printScanRange = printScanRange;
    }

    public static void run(SpringApplication springApplication){
        AopApplicationContext.springApplication = springApplication;
        createInstanceFactory();
        handlerSource(springApplication);
    }



    protected static Set<String> handlerImportHybridRange(){
        Class<AopApplicationContext> contextClass = AopApplicationContext.class;
        ImportHybridRange hybridRange = AnnotationUtils.getAnnotation(contextClass, ImportHybridRange.class);
        Set<String> ps = new HashSet<>();
        for (Class<? extends AopTaskManagerHybrid> hy : hybridRange.value()) {
            ps.add(ClassUtils.getPackageName(hy));
        }
        return ps;
    }

    protected static void handlerSource(SpringApplication application){
        Class<?> applicationClass = application.getMainApplicationClass();
        SpringBootApplication springBootApplication = ChiefApplicationRunner.getAnnotation(SpringBootApplication.class);
        String[] basePackages = springBootApplication.scanBasePackages();
        Class<?>[] classes = springBootApplication.scanBasePackageClasses();
        Set<String> packages = new HashSet<>(Arrays.asList(basePackages));
        for (Class<?> clazz : classes) {
            packages.add( ClassUtils.getPackageName(clazz));
        }
        if (packages.isEmpty()){
            packages.add(ClassUtils.getPackageName(applicationClass));
        }
        packages.addAll(handlerImportHybridRange());
        if (printScanRange && log.isInfoEnabled()) {
            log.info("aop scan basePackages is {}", packages);
        }
        AopApplicationContext.simplePattern = createScannerUtil();
        for (String basePackage : packages) {
            Set<Class<?>> loadClasses;
            loadClasses = ClassSourceCache.getSource(basePackage);
            if (loadClasses == null){
                loadClasses = simplePattern.loadClasses(basePackage);
                ClassSourceCache.registerSource(basePackage, loadClasses);
            }
            AopApplicationContext.source.addAll(loadClasses);
        }
    }

    private static void createInstanceFactory(){
        instanceFactory = FactoryManager.getInstanceFactory();
        if (instanceFactory == null){
            FactoryManager.createInstanceFactory();
            instanceFactory = FactoryManager.getInstanceFactory();
        }
    }

    protected static SimplePattern createScannerUtil(){
        return new IntegratorScanner();
    }

    public static InstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public static SimplePattern getSimplePattern() {
        return simplePattern;
    }

    public static Set<Class<?>> getSource() {
        return source;
    }

    public static SpringApplication getSpringApplication() {
        return springApplication;
    }
}

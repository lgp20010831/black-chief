package com.black.core.api.tacitly;

import com.black.core.api.ApiHttpRestConfigurer;
import com.black.core.util.SimplePattern;
import com.black.core.spring.ChooseScanRangeHolder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ApiObjectDispatcher {

    private final SimplePattern simplePattern;

    private final ApiAliasManger aliasManger;

    private final ApiHttpRestConfigurer apiHttpRestConfigurer;

    public ApiObjectDispatcher(SimplePattern simplePattern, ApiAliasManger aliasManger, ApiHttpRestConfigurer apiHttpRestConfigurer) {
        this.simplePattern = simplePattern;
        this.aliasManger = aliasManger;
        this.apiHttpRestConfigurer = apiHttpRestConfigurer;
    }

    public void scannerObject(){
        /**
         * 扫描所有的实体类
         * 然后将扫描到的每个实体类 class 注册到别名管理中心, 去注册别名
         */
        handlerPojoMutes();

        /**
         * 同样是扫描控制器然后将控制器 class 注册
         * */
        handlerControllerMutes();
    }

    protected void handlerPojoMutes(){
        String[] packages = apiHttpRestConfigurer.pojoScannerPackages();
        if (packages == null){
            throw new RuntimeException("必须指定实体类所在的包时");
        }
        Collection<Class<?>> scannerMutes = obtainScannerMutes(packages);
        scannerMutes.forEach(aliasManger::registerPojo);
    }

    protected void handlerControllerMutes(){
        Collection<Class<?>> scannerMutes = obtainScannerMutes(apiHttpRestConfigurer.scannerPages());
        Set<Class<?>> controllerSet = scannerMutes.stream()
                .filter(this::isController)
                .collect(Collectors.toSet());
        controllerSet.forEach(aliasManger:: registerController);
    }

    protected Collection<Class<?>> obtainScannerMutes(String[] packages){
        Collection<Class<?>> projectClasses = new HashSet<>();
        Collection<String> effectiveRanges = new HashSet<>();
        if (packages == null){
            projectClasses.addAll(simplePattern.loadClasses());
        }else {
            ChooseScanRangeHolder.filterVaildRange(effectiveRanges, packages);
            for (String effectiveRange : effectiveRanges) {
                projectClasses.addAll(simplePattern.loadClasses(effectiveRange));
            }
        }
        return projectClasses;
    }
    public boolean isController(Class<?> clazz){
        return AnnotationUtils.getAnnotation(clazz, Controller.class) != null ||
                AnnotationUtils.getAnnotation(clazz, RestController.class) != null;
    }
}

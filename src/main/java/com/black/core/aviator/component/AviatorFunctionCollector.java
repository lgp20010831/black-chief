package com.black.core.aviator.component;

import com.black.aviator.AviatorManager;
import com.black.core.aviator.annotation.*;
import com.black.core.chain.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.function.AbstractFunction;

import java.util.HashSet;
import java.util.Set;

@LoadSort(94)
@ChainClient(AviatorFunctionCollector.class)
@LazyLoading(EnabledGlobalAviatorLayer.class)
public class AviatorFunctionCollector implements OpenComponent, CollectedCilent, ChainPremise {

    private final Set<AbstractFunction> functionSet = new HashSet<>();

    private final Set<Object> importsClassSet = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        AviatorEvaluatorInstance instance = AviatorManager.getInstance();
        for (AbstractFunction function : functionSet) {
            instance.addFunction(function);
        }

        for (Object o : importsClassSet) {
            Class<?> clazz = (Class<?>) o;
            processClazz(clazz);
        }

        ImportAviatorScan annotation = ChiefApplicationRunner.getAnnotation(ImportAviatorScan.class);
        if (annotation != null){
            ChiefScanner scanner = ScannerManager.getScanner();
            String[] ranges = annotation.value();
            for (String range : ranges) {
                Set<Class<?>> classSet = scanner.load(range);
                classSet.forEach(this::processClazz);
            }
        }
    }

    protected void processClazz(Class<?> clazz){
        AviatorEvaluatorInstance instance = AviatorManager.getInstance();
        ImportStaticFunction importStaticFunction = clazz.getAnnotation(ImportStaticFunction.class);
        if (importStaticFunction != null){
            String name = StringUtils.hasText(importStaticFunction.value()) ? importStaticFunction.value() :  clazz.getSimpleName();
            try {
                instance.addStaticFunctions(name, clazz);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        ImportInstanceFunction importInstanceFunction = clazz.getAnnotation(ImportInstanceFunction.class);
        if (importStaticFunction != null){
            String name = StringUtils.hasText(importInstanceFunction.value()) ? importInstanceFunction.value() :  clazz.getSimpleName();
            try {
                instance.addInstanceFunctions(name, clazz);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("fun", cli -> {
            return BeanUtil.isSolidClass(cli) && AbstractFunction.class.isAssignableFrom(cli) && cli.isAnnotationPresent(AviatorFunction.class);
        });

        register.begin("import", ui -> {
            return BeanUtil.isSolidClass(ui) && ui.isAnnotationPresent(ImportInstanceFunction.class) || ui.isAnnotationPresent(ImportStaticFunction.class);
        }).instance(false);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("fun".equals(resultBody.getAlias())){
            for (Object obj : resultBody.getCollectSource()) {
                functionSet.add((AbstractFunction) obj);
            }

        }

        if ("import".equals(resultBody.getAlias())){
            importsClassSet.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledGlobalAviatorLayer.class);
    }
}

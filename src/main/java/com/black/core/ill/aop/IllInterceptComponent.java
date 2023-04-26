package com.black.core.ill.aop;

import com.black.core.chain.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import org.springframework.core.annotation.AnnotationUtils;


@LoadSort(76)
@ChainClient(IllInterceptComponent.class)
public class IllInterceptComponent implements OpenComponent, CollectedCilent, ChainPremise {

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("error", true, hyu ->{
            return IllHandler.class.isAssignableFrom(hyu) && BeanUtil.isSolidClass(hyu) &&
                    AnnotationUtils.getAnnotation(hyu, GlobalThrowableHandler.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("error".equals(resultBody.getAlias())){
            GlobalThrowableHandlerManagement.registerAll(StreamUtils.mapList(resultBody.getCollectSource(), ele -> (IllHandler)ele));
        }
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

    }

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        if(mainClass != null){
            return AnnotationUtils.getAnnotation(mainClass, EnabledGlobalThrowableManagement.class) != null;
        }
        return false;
    }
}

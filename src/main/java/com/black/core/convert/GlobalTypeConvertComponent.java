package com.black.core.convert;

import com.black.core.cache.TypeConvertCache;
import com.black.core.chain.*;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.ClosableSort;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.utils.DefaultMappingKeyHandler;
import com.black.utils.MappingKeyHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.Set;

@LoadSort(-5)
@ChainClient
@AddHolder
@ClosableSort(4500)
@Adaptation(GetTypeComponentAdapter.class)
public class GlobalTypeConvertComponent implements OpenComponent, CollectedCilent, ApplicationDriver {

    public static final String ALIAS = "typeConvert";

    private MappingKeyHandler mappingKeyHandler;

    private TypeHandler typeHandler;

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        clearCache(typeHandler);
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        mappingKeyHandler = new DefaultMappingKeyHandler();
        if (TypeConvertCache.existHandler()){
            typeHandler = TypeConvertCache.getTypeHandler();
        }else {
            typeHandler = new TypeHandler(mappingKeyHandler);
            TypeConvertCache.registerTypeHandler(typeHandler);
        }
    }


    public void scan(Set<Class<?>> types){
        Set<Class<?>> filterSet = StreamUtils.filterSet(types, cl -> AnnotationUtils.getAnnotation(cl, TypeContributor.class) != null &&
                BeanUtil.isSolidClass(cl));
        FactoryManager.init();
        InstanceFactory factory = FactoryManager.getInstanceFactory();
        Set<?> objects = StreamUtils.mapSet(filterSet, factory::getInstance);
        if (typeHandler == null){
            load(null);
        }
        typeHandler.parse((Collection<Object>) objects);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin();
        entry.setAlias(ALIAS);
        entry.needOrder(false);
        entry.condition(j ->{
            return AnnotationUtils.getAnnotation(j, TypeContributor.class) != null &&
                    BeanUtil.isSolidClass(j);
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (ALIAS.equals(resultBody.getAlias())){
            typeHandler.parse(resultBody.getCollectSource());
        }
    }
}

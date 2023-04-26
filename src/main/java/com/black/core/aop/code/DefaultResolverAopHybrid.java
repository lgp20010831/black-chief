package com.black.core.aop.code;

import com.black.core.aop.annotation.AopHybrid;
import com.black.core.spring.instance.InstanceFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultResolverAopHybrid implements ResolverAopHybrid {
    @Override
    public Collection<AopTaskManagerHybrid> handlerAopHybrids(Set<Class<?>> source, AbstractAopTaskQueueAdapter queueAdapter) {

        Collection<AopTaskManagerHybrid> hybrids = new HashSet<>();
        InstanceFactory instanceFactory = AopApplicationContext.getInstanceFactory();
        for (Class<?> clazz : source) {
            AopHybrid aopHybrid;
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ||
                    clazz.isEnum() || !AopTaskManagerHybrid.class.isAssignableFrom(clazz) ||
                    (aopHybrid = AnnotationUtils.getAnnotation(clazz, AopHybrid.class)) == null){
                continue;
            }
            Class<? extends Premise> premise = aopHybrid.value();
            if (!premise.equals(Premise.class)){
                Premise instance = instanceFactory.getInstance(premise);
                if (!instance.condition(queueAdapter)) {
                    continue;
                }
            }
            hybrids.add((AopTaskManagerHybrid) instanceFactory.getInstance(clazz));
        }
        return hybrids;
    }
}

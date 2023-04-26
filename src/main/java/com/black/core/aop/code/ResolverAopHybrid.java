package com.black.core.aop.code;

import java.util.Collection;
import java.util.Set;

public interface ResolverAopHybrid {


    Collection<AopTaskManagerHybrid> handlerAopHybrids(Set<Class<?>> source, AbstractAopTaskQueueAdapter queueAdapter);

}

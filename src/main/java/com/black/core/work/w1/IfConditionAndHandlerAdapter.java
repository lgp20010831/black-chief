package com.black.core.work.w1;

import com.black.core.spring.instance.InstanceFactory;
import com.black.core.work.w1.cache.CacheTask;

import java.util.*;

@SuppressWarnings("all")
public class IfConditionAndHandlerAdapter {


    final CacheTask cacheTask;
    final List<TaskNodeResolver<Boolean>> resolvers;
    final InstanceFactory instanceFactory;
    public IfConditionAndHandlerAdapter(CacheTask cacheTask,
                                        List<TaskNodeResolver<Boolean>> resolvers, InstanceFactory instanceFactory) {
        this.cacheTask = cacheTask;
        this.resolvers = resolvers;
        this.instanceFactory = instanceFactory;
    }


    public WorkFlowTemplateBuilder begin(){
        return new WorkFlowTemplateBuilder(cacheTask, resolvers, instanceFactory);
    }

}

package com.black.core.aop.servlet.plus.handler;

import com.black.core.util.IntegratorScanner;
import com.black.core.util.SimplePattern;
import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.instance.InstanceFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlusResolverScheduler {


    private final SimplePattern simplePattern;

    private List<PlusVariableResolver> variableResolvers;

    public static final String RESOLVER_PACKAGE = "com.example.springautothymeleaf.aop.servlet.plus.handler";

    public PlusResolverScheduler() {
        simplePattern = new IntegratorScanner();
    }

    public List<PlusVariableResolver> getVariableResolvers(){
        if (variableResolvers == null){
            variableResolvers = new ArrayList<>();
            Set<Class<?>> classSet = simplePattern.loadClasses(RESOLVER_PACKAGE);
            for (Class<?> c : classSet) {
                if (c.isInterface() || c.isEnum() || Modifier.isAbstract(c.getModifiers()) || c.equals(PlusResolverScheduler.class)){
                    continue;
                }

                ChiefExpansivelyApplication expansivelyApplication = ChiefApplicationHolder.getExpansivelyApplication();
                InstanceFactory instanceFactory = expansivelyApplication.instanceFactory();
                if (PlusVariableResolver.class.isAssignableFrom(c)){
                    Object instance = instanceFactory.getInstance(c);
                    variableResolvers.add((PlusVariableResolver) instance);
                }
            }
        }
        return variableResolvers;
    }

}

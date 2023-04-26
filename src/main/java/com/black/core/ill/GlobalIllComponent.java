package com.black.core.ill;


import com.black.core.chain.ChainClient;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.ConditionResultBody;
import com.black.core.chain.QueryConditionRegister;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflexHandler;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

@IgnorePrint
@LoadSort(456)
@ChainClient
public class GlobalIllComponent implements CollectedCilent, OpenComponent {

    private final Collection<ThrowableResolverWrapper> resolverWrappers = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        GlobalThrowableCentralizedHandling instance = GlobalThrowableCentralizedHandling.getInstance();
        instance.registerWrappers(resolverWrappers);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("manager", cla -> BeanUtil.isSolidClass(cla) &&
                AnnotationUtils.getAnnotation(cla, IllManagement.class) != null);

        register.begin("resolver", cla -> BeanUtil.isSolidClass(cla) && ThrowableResolver.class.isAssignableFrom(cla) &&
                AnnotationUtils.getAnnotation(cla, IllResolver.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("manager".equals(resultBody.getAlias())){
            for (Object manager : resultBody.getCollectSource()) {
                for (Method method : ReflexHandler.getAccessibleMethods(manager)) {
                    if (method.getParameterCount() == 1 && method.getReturnType().equals(void.class) &&
                    Throwable.class.isAssignableFrom(method.getParameterTypes()[0])){
                        IllResolver illResolver = AnnotationUtils.getAnnotation(method, IllResolver.class);
                        if (illResolver != null){
                            resolverWrappers.add(new ThrowableResolverWrapper(
                               new ResolverSubstitute(method, manager), illResolver.value(), illResolver.asyn()
                            ));
                        }
                    }
                }
            }
        }

        if ("resolver".equals(resultBody.getAlias())){
            for (Object resolver : resultBody.getCollectSource()) {
                IllResolver illResolver = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(resolver), IllResolver.class);
                resolverWrappers.add(
                        new ThrowableResolverWrapper((ThrowableResolver) resolver, illResolver.value(), illResolver.asyn())
                );
            }
        }
    }



    public static class ResolverSubstitute implements ThrowableResolver{

        private final Method method;
        private final Object obj;

        public ResolverSubstitute(@NonNull Method method, @NonNull Object obj) {
            this.method = method;
            this.obj = obj;
        }

        @Override
        public void doResolve(Throwable ex) throws Throwable {
            method.invoke(obj, ex);
        }
    }
}

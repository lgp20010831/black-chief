package com.black.core.native_sql;

import com.black.holder.SpringHodler;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;
import com.black.sql.NativeMapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@LoadSort(7858)
@LazyLoading(EnabledNativeAllocationStrategy.class)
public class NativeSQLComponent implements OpenComponent {

    private static final Collection<NativeBlendSupportResolver> supportResolvers = new HashSet<>();

    static {
        supportResolvers.add(new MapSqlNativeBlendResolver());
        supportResolvers.add(new SpringNativeBlendResolver());
    }

    public static void registerResolver(NativeBlendSupportResolver resolver){
        supportResolvers.add(resolver);
    }

    public static Collection<NativeBlendSupportResolver> getSupportResolvers() {
        return supportResolvers;
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {
        Class<?> mainClass = expansivelyApplication.getStartUpClazz();
        EnabledNativeAllocationStrategy strategy = AnnotationUtils.findAnnotation(mainClass, EnabledNativeAllocationStrategy.class);
        Assert.notNull(strategy, "strategy is null");
        String strategyValue = strategy.value();
        parseStrategy(strategyValue);
    }

    private void parseStrategy(String strategy){
        List<BlendObject> blendObjects = CharParser.parseBlend(strategy);
        if (blendObjects.isEmpty()){
            throw new IllegalStateException("unknown blent strategy");
        }
        for (BlendObject blendObject : blendObjects) {
            resolveBlendObject(blendObject);
        }
    }

    private void resolveBlendObject(BlendObject blendObject){
        String name = blendObject.getName();
        Collection<NativeBlendSupportResolver> supportResolvers = getSupportResolvers();
        for (NativeBlendSupportResolver supportResolver : supportResolvers) {
            if (supportResolver.support(name)) {
                for (String attribute : blendObject.getAttributes()) {
                    doResolveDatasource(supportResolver, attribute);
                }
            }
        }
    }

    private void doResolveDatasource(NativeBlendSupportResolver resolver, String attribute){
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringHodler.getBeanFactory();
        Assert.notNull(beanFactory, "can not find spring bean factory");
        ReusingProxyFactory proxyFactory = FactoryManager.initAndGetProxyFactory();
        try {
            TransactionHandlerAndDataSourceHolder sourceHolder = resolver.obtainDataSource(attribute);
            NativeMapper proxy = proxyFactory.prototypeProxy(NativeMapper.class, new NativeMapperProxy(sourceHolder));
            String beanName = sourceHolder.getBeanName();
            Assert.notNull(beanName, "unknown native mapper bean name");
            beanFactory.registerSingleton(beanName, proxy);
        } catch (Throwable e) {
            throw new IllegalStateException("resolver: " + resolver + " obtain datasource fair", e);
        }
    }
}

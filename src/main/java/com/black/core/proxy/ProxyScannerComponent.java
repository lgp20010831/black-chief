package com.black.core.proxy;

import com.black.holder.SpringHodler;
import com.black.core.util.SimplePattern;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.driver.PostBeforeBeanInstantiationDriver;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.InstanceFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ProxyScannerComponent implements OpenComponent, PostPatternClazzDriver, EnabledControlRisePotential,
        PostBeforeBeanInstantiationDriver, ApplicationDriver {

    private AutomaticProxyInjectionConfiguration automaticProxyInjectionConfiguration;

    private InstanceFactory instanceFactory;

    private ReusingProxyFactory reusingProxyFactory;

    private SimplePattern simplePattern;

    private final Map<AgentLayer, Collection<Class<?>>> forSpringProxy = new HashMap<>();

    private final Map<AgentLayer, Collection<Class<?>>> instanceProxy = new HashMap<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        integrateData();
        for (AgentLayer agentLayer : forSpringProxy.keySet()) {
            for (Class<?> c : forSpringProxy.get(agentLayer)) {
                if (!reusingProxyFactory.isAgent(c)) {
                    expansivelyApplication.reusingProxy(c, agentLayer);
                }
            }
        }

        for (AgentLayer agentLayer : instanceProxy.keySet()) {
            for (Class<?> c : instanceProxy.get(agentLayer)) {
                reusingProxyFactory.proxy(c, agentLayer);
            }
        }
    }

    @Override
    public void postPatternClazz(Class<?> beanClazz, Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                 ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {

        if (AgentLayer.class.isAssignableFrom(beanClazz)){
            Layer layer;
            if ((layer = AnnotationUtils.getAnnotation(beanClazz, Layer.class)) != null){
                if (simplePattern == null){
                    if (instanceFactory == null){
                        instanceFactory = chiefExpansivelyApplication.instanceFactory();
                    }
                    simplePattern = instanceFactory.getInstance(SimplePattern.class);
                }
                AgentLayer agentLayer = instanceLayer(beanClazz);
                Collection<Class<?>> targetStream = new ArrayList<>();
                String[] packages = layer.scannerPackages();
                boolean lazy = layer.lazyForSpring();
                if (packages.length != 0){
                    for (String p : packages) {
                        if ("".equals(p)){
                            continue;
                        }
                        Set<Class<?>> classes = simplePattern.loadClasses(p);
                        if (classes != null && !classes.isEmpty()){
                            targetStream.addAll(classes);
                        }

                    }
                }
                Class<?>[] value = layer.value();
                targetStream.addAll(Arrays.asList(value));
                List<Class<?>> afterDistinct = targetStream.stream().distinct().collect(Collectors.toList());
                Collection<Class<?>> list = lazy ? forSpringProxy.computeIfAbsent(agentLayer, k -> new ArrayList<>())
                        : instanceProxy.computeIfAbsent(agentLayer, ke -> new ArrayList<>());
                list.addAll(afterDistinct);
            }
        }
    }

    @Override
    public void whenApplicationStart(ChiefExpansivelyApplication application) {
        Collection<Object> configurationMutes = application.getApplicationConfigurationMutes();
        instanceFactory = application.instanceFactory();
        reusingProxyFactory = application.obtainReusingProxyFactory();
        createConfig();
        handlerConfig(configurationMutes);
        for (Object proxy : reusingProxyFactory.getProxyInstanceCache()) {
            SpringHodler.getListableBeanFactory().autowireBean(proxy);
        }
    }

    private void integrateData(){
        Map<AgentLayer, Collection<String>> scopes = automaticProxyInjectionConfiguration.getScopes();
        Map<AgentLayer, Collection<TargetWrapper>> proxyCache = automaticProxyInjectionConfiguration.getProxyCache();
        for (AgentLayer agentLayer : scopes.keySet()) {
            Collection<Class<?>> set = forSpringProxy.computeIfAbsent(agentLayer, k -> new ArrayList<>());
            for (String pack : scopes.get(agentLayer)) {
                if (!"".equals(pack)){
                    for (Class<?> clazz : simplePattern.loadClasses(pack)) {
                        if (!set.contains(clazz)){
                            set.add(clazz);
                        }
                    }
                }
            }
        }

        proxyCache.forEach((k, v) ->{
            Collection<Class<?>> set = forSpringProxy.computeIfAbsent(k, key -> new ArrayList<>());
            Collection<Class<?>> instanceSet = instanceProxy.computeIfAbsent(k, key2 -> new ArrayList<>());
            for (TargetWrapper targetWrapper : v) {
                Class<?> targetClazz = targetWrapper.getTargetClazz();
                if (targetWrapper.isLazyForSpring()){
                    if (!set.contains(targetClazz)){
                        set.add(targetClazz);
                    }
                }else {
                    if (!instanceSet.contains(targetClazz)){
                        instanceSet.add(targetClazz);
                    }
                }
            }
        });
    }

    private AgentLayer instanceLayer(Class<?> beanClass){
        return (AgentLayer) instanceFactory.getInstance(beanClass);
    }

    @Override
    public Class<? extends Annotation> registerEnableAnnotation() {
        return EnableProxy.class;
    }

    private void handlerConfig(Collection<Object> configurationMutes){
        ProxyConfigurerAdapter configurerAdapter = new ProxyConfigurerAdapter();
        for (Object configurationMute : configurationMutes) {
            if (configurationMute instanceof ProxyConfigurer){
                ProxyConfigurer proxyConfigurer = (ProxyConfigurer) configurationMute;
                automaticProxyInjectionConfiguration.putAllCache(proxyConfigurer.packageAgent(configurerAdapter));
                automaticProxyInjectionConfiguration.putAllTarget(proxyConfigurer.registerTarget(configurerAdapter));
            }
        }
    }

    private void createConfig(){
        automaticProxyInjectionConfiguration = instanceFactory.getInstance(AutomaticProxyInjectionConfiguration.class);
    }

    /** 索引 */
    private final Map<Class<?>, Collection<AgentLayer>> proxyIndexing = new HashMap<>();

    @Override
    public Object postBeforeBeanInstantiationLogic(Class<?> beanClass, String beanName,
                                                   ChiefExpansivelyApplication chiefExpansivelyApplication,
                                                   Object previousResultBean, PostBeforeBeanInstantiationDriver previousDriver) {
        if (proxyIndexing.isEmpty()){
            for (AgentLayer agentLayer : forSpringProxy.keySet()) {
                Collection<Class<?>> value = forSpringProxy.get(agentLayer);
                for (Class<?> clazz : value) {
                    Collection<AgentLayer> array = proxyIndexing.computeIfAbsent(clazz, k -> new ArrayList<>());
                    array.add(agentLayer);
                }
            }
        }
        final ReusingProxyFactory proxyFactory = chiefExpansivelyApplication.obtainReusingProxyFactory();
        if (proxyIndexing.containsKey(beanClass)){
            if (previousResultBean == null){
                for (AgentLayer layer : proxyIndexing.get(beanClass)) {
                    previousResultBean = chiefExpansivelyApplication.reusingProxy(beanClass, layer);
                }
                return previousResultBean;
            }else {
                if (!proxyFactory.isAgent(beanClass)) {
                    if (log.isWarnEnabled()) {
                        log.warn("proxy object already If it is constructed or" +
                                " represented elsewhere, it may cause conflict");
                    }
                }else {
                    for (AgentLayer layer : proxyIndexing.get(beanClass)) {
                        previousResultBean = chiefExpansivelyApplication.reusingProxy(beanClass, layer);
                    }
                }
            }
        }
        return previousResultBean;
    }
}

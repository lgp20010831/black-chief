package com.black.core.spring;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.NotNull;
import com.black.core.spring.annotation.ClosableSort;
import com.black.core.spring.annotation.InstanceShape;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.driver.Driver;
import com.black.core.spring.driver.FilterComponent;
import com.black.core.spring.driver.PostComponentInstance;
import com.black.core.spring.event.ApplicationEvent;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.spring.instance.DefaultInstanceElementFactory;
import com.black.core.spring.instance.InstanceElementFactory;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.util.*;


@Log4j2 @SuppressWarnings("all")
public abstract class AbstractEmbedApplication implements ChiefExpansivelyApplication{

    protected boolean isRunning;

    /***
     * 当前应用的状态
     */
    private volatile ApplicationEvent happendEvent;


    protected volatile boolean loaded = false;

    /***
     * application 的配置类
     */
    protected PettyApplicationConfiguration applicationConfiguration;

    /** 启动类 */
    protected Class<?> mainClass;

    /**
     * 用于实例化扫描到的组件 class 对象
     * 如果要实例化的 class 需要依赖其他组件, 可以
     * 通过{@link InstanceFactory} 规则, {@link com.black.core.spring.instance.Reflex}
     * 来标注其他组件实现注入
     */
    protected InstanceFactory instanceFactory;

    protected Class<? extends ChiefScanner> scannerType;

    /**
     * 用来创建代理对象, 向组建提供一个可以创建代理的工厂
     * 但需要提前判断工厂是否存在,因为没有默认工厂
     */
    protected volatile SpringProxyFactory proxyFactory;


    protected volatile ReusingProxyFactory reusingProxyFactory;

    /***
     * 项目中扫描到的所有 class 对象
     */
    protected final Collection<Class<?>> projectClasses = new HashSet<>(256);

    /**
     * 黑名单
     */
    protected final Collection<Class<?>> refuseComponents = new HashSet<>();

    /***
     * 组件的缓存, 无论实现了{@link OpenComponent} 或者{@link Driver}
     * 都会被实例化然后加入到这个组件的缓存, 存在的目的其实是为了方法
     * spring 与 componentCache 实例重复
     *
     *  Component caching, whether implemented {@link OpenComponent} or {@link Driver}
     * Will be instantiated and added to the cache of this component. In fact, the purpose of existence is for methods
     * Duplicate spring and componentcache instances
     */
    protected final Map<Class<?>, Object> componentCache = new HashMap<>(128);

    public AbstractEmbedApplication(Object configuration){
        adjustmentEventWithCreate();
        try {

            initFactory();

            obtainReusingProxyFactory();

            //load configuration
            initConfiguration(configuration);
        }finally {
            endCreate();
        }

    }

    @Override
    public boolean isLoad() {
        return loaded;
    }

    @Override
    public <A extends Annotation> A getAnnotationByMainClass(Class<A> type) {
        return AnnotationUtils.findAnnotation(getStartUpClazz(), type);
    }

    public Class<?> getStartUpClazz(){

        if (mainClass != null){
            return mainClass;
        }
        return BeanUtil.getPrimordialClass(applicationConfiguration.getRelyOnConfiguration());
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setScannerType(Class<? extends ChiefScanner> scannerType) {
        this.scannerType = scannerType;
    }

    public void setMainClass(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    protected List<ApplicationDriver> sortClosableDriver(Collection<ApplicationDriver> drivers){
        List<Object> list = SQLUtils.wrapList(drivers);
        Object sort = ServiceUtils.sort(list, ele -> {
            Class<?> primordialClass = BeanUtil.getPrimordialClass(ele);
            ClosableSort annotation = primordialClass.getAnnotation(ClosableSort.class);
            return annotation == null ? 0 : annotation.value();
        }, true);
        return (List<ApplicationDriver>) sort;
    }

    public abstract void scanMutes();

    @Override
    public boolean isShutdown() {
        return !isRunning;
    }

    @Override
    public void shutdown() {
        if (isShutdown()) {
            return;
        }
        isRunning = false;
        if (log.isInfoEnabled()){
            log.info("ChiefExpansivelyApplication stop...");
        }
        Collection<ApplicationDriver> drivers = obtainApplicationDrivers();
        if (drivers != null && !drivers.isEmpty()){
            List<ApplicationDriver> sortDrivers = sortClosableDriver(drivers);
            for (ApplicationDriver driver : sortDrivers) {
                driver.whenApplicationStop(this);
            }
        }
        System.gc();
    }

    @Override
    public void applicationStart() {
        isRunning = true;
        if (log.isInfoEnabled()){
            log.info("ChiefExpansivelyApplication start...");
        }
        Collection<ApplicationDriver> drivers = obtainApplicationDrivers();
        if (drivers != null && !drivers.isEmpty()){
            for (ApplicationDriver applicationDriver : drivers) {
                applicationDriver.whenApplicationStart(this);
            }
        }
    }


    /***
     * 这个方法是在实例化以后调用的
     * 在构造方法中将所有的 class对象都进行的加载
     * 可能的问题, 如果有对象加入到 spring 容器中了
     *
     */
    public void pattern(){
        adjustmentEventWithPattern();

        //scan all class
        scanMutes();

        /** 实例化所有加入到缓存的对象 */
        instanceAllElment();
    }

    public abstract void instanceAllElment();

    @Override
    public Object refuseComponent(Class<?> componentClass) {
        return refuseComponents.add(componentClass);
    }

    @Override
    public boolean registerComponentInstance(OpenComponent openComponent) {
        Class<? extends OpenComponent> clazz = openComponent.getClass();
        if (refuseComponents.contains(clazz)){
            return false;
        }
        Map<Class<? extends OpenComponent>, Object> openComponents = obtainOpenComponents();
        if (!openComponents.containsKey(clazz)) {
            openComponents.put(clazz, openComponent);
            return true;
        }
        return false;
    }




    public Object instanceComponent(Class<?> beanClass){
        if (beanClass == null){
            return null;
        }
        if (refuseComponents.contains(beanClass)){
            if (log.isDebugEnabled()) {
                log.debug("该注册的用户在拒绝队列中存在, 拒绝注册");
            }
            return null;
        }
        /*
            首先要通过实例化工厂实例该对象
            然后经过一个处理链表, 最终加入到缓存中
         */
        Collection<Class<? extends Driver>> postComponentDrivers = obtainDriverInitializationCache().get(PostComponentInstance.class);
        if (postComponentDrivers != null && !postComponentDrivers.isEmpty()){
            final Collection<Class<? extends Driver>> postComponentDriverClazzs = ApplicationUtil.clone(postComponentDrivers);
            postComponentDrivers.clear();
            for (Class<? extends Driver> driverClazz : postComponentDriverClazzs) {
                Object component = instanceComponent(driverClazz);
                obtainDriverInstanceMutes()
                        .computeIfAbsent(PostComponentInstance.class, k -> new ArrayList<>())
                        .add((Driver) component);
            }
        }

        //初始化过滤器组件
        Collection<Class<? extends Driver>> filterMutes = obtainDriverInitializationCache().get(FilterComponent.class);
        if (filterMutes != null && !filterMutes.isEmpty()){
            final Collection<Class<? extends Driver>> filterDriverClazzMutes = ApplicationUtil.clone(filterMutes);
            filterMutes.clear();
            for (Class<? extends Driver> filterDriverClazzMute : filterDriverClazzMutes) {
                Object component = instanceComponent(filterDriverClazzMute);
                filterComponentMutes()
                        .add((FilterComponent) component);
            }
        }

        Collection<FilterComponent> filterComponents = filterComponentMutes();
        if (filterComponents != null && !filterComponents.isEmpty()){
            for (FilterComponent filterComponent : filterComponents) {
                if (filterComponent.filter(beanClass, this)) {
                    if (log.isDebugEnabled()) {
                        log.debug("过滤器组件:{} -- 过滤要注册的组件:{} ",
                                filterComponent.getClass().getSimpleName(), beanClass.getSimpleName());
                    }
                    return null;
                }
            }
        }

           /*
            首先要通过实例化工厂实例该对象
            然后经过一个处理链表, 最终加入到缓存中
         */
        Collection<PostComponentInstance> drivers = postComponentInstance();
        if (drivers != null && !drivers.isEmpty()){
            for (PostComponentInstance componentInstanceDriver : drivers) {
                Object component = componentInstanceDriver.beforeInstance(beanClass, this);
                if (component != null){
                    doAfterInstance(beanClass, component, drivers);
                    return component;
                }
            }
        }

        Object instance = createInstance(beanClass);
        return doAfterInstance(beanClass, instance, drivers);
    }

    @Override
    public Object registerComponent(@NotNull Class<? extends OpenComponent> componentClass) {
        Object component = instanceComponent(componentClass);
        if (component != null){
            registerComponentToCache(componentClass, component);
        }
        return component;
    }

    private Object doAfterInstance(@NotNull Class<?> componentClass, Object bean, Collection<PostComponentInstance> drivers){
        if (drivers != null && !drivers.isEmpty()){
            for (PostComponentInstance componentInstanceDriver : drivers) {
                Object instance = componentInstanceDriver.afterInstance(componentClass,  bean, this);
                if (instance == null){
                    if (log.isDebugEnabled()) {
                        log.debug("执行组件后置处理后, driver: {}, 将组件: {}过滤", componentInstanceDriver, componentClass);
                    }
                    refuseComponent(componentClass);
                    return instance;
                }
            }
        }
        return bean;
    }


    private void registerComponentToCache(Class<? extends OpenComponent> componentClass, Object bean){
        Map<Class<? extends OpenComponent>, Object> openComponents = obtainOpenComponents();
        openComponents.put(componentClass, bean);
    }

    protected void initConfiguration(Object configuration){
        applicationConfiguration = createConfiguration();
        applicationConfiguration.setRelyOnConfiguration(configuration);
        applicationConfiguration.init();
    }

    protected PettyApplicationConfiguration createConfiguration(){
        return instanceFactory.getInstance(PettyApplicationConfiguration.class);
    }

    private void endCreate(){}
    //*************************************** proxy **********************************
    @Override
    public ReusingProxyFactory obtainReusingProxyFactory() {
        reusingProxyFactory = FactoryManager.getProxyFactory();
        if (reusingProxyFactory == null){
            FactoryManager.createDefaultProxyFactory();
            reusingProxyFactory = FactoryManager.getProxyFactory();
        }
        return reusingProxyFactory;
    }

    public ReusingProxyFactory getReusingProxyFactory(){
        return reusingProxyFactory;
    }

    @Override
    public <T> T reusingProxy(Class<T> beanClazz, AgentLayer agentLayer) {
        if (reusingProxyFactory != null){
            return reusingProxyFactory.proxy(beanClazz, agentLayer);
        }
        return null;
    }

    public void setProxyFactory(SpringProxyFactory springProxyFactory)      {proxyFactory = springProxyFactory;}
    public SpringProxyFactory proxyFactory()                                {return proxyFactory;}
    public boolean actAgent()                                               {return proxyFactory != null;}
    @Override
    public PettyApplicationConfiguration obtainConfiguration() {
        return applicationConfiguration;
    }

    @Override
    public Map<Class<?>, Object> getComponentMutes() {
        return componentCache;
    }



    public void run(){
        pattern();
        run0();
    }

    public void run0(){
        try {

            //Adjust the current application status to load
            adjustmentEventWithLoad();
            if (!cancel()){
                pareLoad();
                doOnApplicationEvent();
            }
        }finally {
            endRun();
        }
    }

    protected boolean cancel(){
        if (applicationConfiguration.isCancelLoad()) {
            if (log.isDebugEnabled()) {
                log.debug("cancel pettyApplication load");
            }
            return true;
        }
        return false;
    }

    public ApplicationEvent getEvent(){
        return happendEvent;
    }

    public void endRun(){
        adjustmentEventWithDormancy();
    }

    private void adjustmentEventWithLoad(){
        happendEvent = ApplicationEvent.load;
    }

    protected void adjustmentEventWithDistory(){
        happendEvent = ApplicationEvent.distory;
    }

    private void adjustmentEventWithCreate(){
        happendEvent = ApplicationEvent.create;
    }

    private void adjustmentEventWithPattern(){
        happendEvent = ApplicationEvent.pattern;
    }

    private void adjustmentEventWithDormancy(){
        happendEvent = ApplicationEvent.dormancy;
    }

    public Collection<Class<?>> getProjectClasses() {
        return projectClasses;
    }

    //**************************  handler bean mutes *********************

    /**
     * Object obj;
     *         Constructor<?> constructor = proxyFactory.findConstructor(beanClass);
     *         try {
     *             obj = ObjectFactory.newObject(beanClass, constructor.getParameterTypes(),
     *                     proxyFactory.returnArgs(constructor, beanFactory));
     *         } catch (Exception e) {
     *             throw new RuntimeException("create obj:" + beanClass + " fail", e);
     *         }
     *         return obj;
     * @param beanClass bean class
     * @return bean
     */
    public <T> T createInstance(@NotNull Class<T> beanClass){
        InstanceType type = selectInstanceType();
        InstanceShape annotation = beanClass.getAnnotation(InstanceShape.class);
        if (annotation != null){
            type = annotation.value();
        }
        return InstanceBeanManager.instance(beanClass, type);
    }

    protected InstanceType selectInstanceType(){
        return InstanceType.INSTANCE;
    }

    protected void initFactory(){
        instanceFactory = obtainInstanceFactory();
        instanceFactory.registerInstance(AbstractEmbedApplication.class, this);
    }

    public InstanceFactory obtainInstanceFactory(){
        instanceFactory = FactoryManager.getInstanceFactory();
        if (instanceFactory == null){
            FactoryManager.createInstanceFactory();
            instanceFactory = FactoryManager.getInstanceFactory();
        }
        //LightnessInstanceFactory instanceFactory = new LightnessInstanceFactory(obtainInstanceElementFactory());
        return instanceFactory;
    }

    public InstanceElementFactory obtainInstanceElementFactory(){
        return new DefaultInstanceElementFactory();
    }

    public ChiefScanner obtainScanner(){
        return ScannerManager.getScanner(scannerType);
    }

    @Override
    public InstanceFactory instanceFactory() {
        return instanceFactory;
    }

    public abstract void doOnApplicationEvent();

    protected abstract void pareLoad();
}

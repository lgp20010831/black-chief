package com.black.core.factory.beans;

import com.black.bin.ApplyProxyFactory;
import com.black.condition.ConditionalEngine;
import com.black.condition.ConditionalHodler;
import com.black.condition.def.DefaultConditionalOnClass;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.agent.ApplyBeanProxy;
import com.black.core.factory.beans.agent.BeanProxy;
import com.black.core.factory.beans.agent.ProxyType;
import com.black.core.factory.beans.config.BeanFactoryConditionalOnClass;
import com.black.core.factory.beans.lazy.LazyFactory;
import com.black.core.factory.beans.process.inter.*;
import com.black.core.json.ReflexUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.tools.BeanUtil;
import com.black.utils.NameUtil;
import com.black.utils.ReflexHandler;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

@Log4j2 @SuppressWarnings("all")
public abstract class AbstractBeanFactory implements BeanFactory {

    /**
     * A processor that stores all objects of operation
     */
    protected final Collection<BeanPostProcessor> beanPostProcessors = new LinkedBlockingQueue<>();

    /**
     * Store all the places that help the factory realize its functions
     */
    protected final Collection<BeanFactoryProcessor> factoryProcessors = new LinkedBlockingQueue<>();

    /**
     * control prototype creation lock
     */
    protected final Map<Class<?>, ReentrantLock> prototypeLocks = new ConcurrentHashMap<>();

    /**
     * Identity definition of storage object
     */
    protected final Map<Class<?>, BeanDefinitional<?>> definitionalCache = new ConcurrentHashMap<>();

    /**
     * Store the factory object initialized by the object
     */
    protected final Map<Class<?>, InitializeFactoryBean> initializeFactoryBeanMutes = new ConcurrentHashMap<>();

    /**
     * Place for storing class packages wrapper
     */
    protected final Map<Class<?>, ClassWrapper<?>> classWrapperCache = new ConcurrentHashMap<>();

    /**
     * cache for storing mapping relationships
     */
    protected final Map<Class<?>, List<Class<?>>> classMapping = new ConcurrentHashMap<>();

    /**
     * Cache where the final instance is located
     */
    protected final Map<Class<?>, Object> instanceMutes = new ConcurrentHashMap<>();

    /**
     * create queue
     */
    protected final Set<Class<?>> creatingQueue = new HashSet<>();

    /** lazy loading */
    private ConditionalEngine conditionalEngine;

    /** to string flush ? */
    public static volatile boolean flushString = false;

    //无参构造
    public AbstractBeanFactory(){}

    @Override
    public <T> T replaceInstance(Class<T> type, @NonNull T newBean) {
        T singleBean = getSingleBean(type);
        Map<Class<?>, Object> mutes = getMutes();
        mutes.replace(type, newBean);
        return singleBean;
    }

    /***
     * This method is freely defined by subclasses.
     * The return type and parameter type are
     * not based on the principle of taking class as
     * the only parameter
     * @param param Any defined type
     * @return Any type of object
     */
    @Override
    public Object get(Object param) {
        if(param instanceof Class){
            return getSingleBean((Class<?>) param);
        }
        throw new BeanFactoryUnsupportException("param type should is class");
    }

    /***
     * Get objects, According to class type
     * @param genealogyClass All class objects on the implementation
     *                       and inheritance chain
     * @param <B> class type
     * @return All objects that match this type
     */
    @Override
    public <B> List<B> getBean(Class<B> genealogyClass) {
        return getBean(genealogyClass, null);
    }

    /***
     * Get an object, According to class type
     * @param genealogyClass All class objects on the implementation
     *                       and inheritance chain
     * @param <B> class type
     * @return Get a singleton object. If there are multiple objects,
     *         select one at random
     */
    @Override
    public <B> B getSingleBean(Class<B> genealogyClass) {
        return getSingleBean(genealogyClass, null);
    }

    /***
     * Get objects, According to class type
     * @param genealogyClass All class objects on the implementation
     *                      and inheritance chain
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @param <B> class type
     * @return All objects that match this type
     */
    @Override
    public <B> List<B> getBean(Class<B> genealogyClass, Map<String, Object> tempSource) {
        final List<B> beans = new ArrayList<>();
        final List<Class<?>> list = classMapping.get(genealogyClass);
        if (list == null || list.isEmpty()){

            //to create objects
            B bean0 = getBean0(genealogyClass, tempSource);
            beans.add(bean0);
        }else {

            //cache exists
            for (Class<?> type : list) {
                Object bean0 = getBean0(type, tempSource);
                beans.add((B) bean0);
            }
        }
        return beans;
    }

    public <B> B getBean0(Class<B> genealogyClass, Map<String, Object> tempSource){
        synchronized (instanceMutes){
            Object completeBean = instanceMutes.get(genealogyClass);
            if (completeBean == null){
                InitializeFactoryBean initializeFactoryBean = initializeFactoryBeanMutes.get(genealogyClass);
                if (initializeFactoryBean == null){
                    try {

                        B bean = createBean(genealogyClass, tempSource);
                        registerBean(bean);
                        if (log.isDebugEnabled()) {
                            log.debug("The factory successfully created an " +
                                    "object and registered it inside the factory of [{}]", bean);
                        }
                        return bean;
                    }catch (BeanFactorysException factorysError){
                        if (log.isErrorEnabled()) {
                            log.error("error for create bean");
                        }
                        throw new BeanFactorysException("exception trying to get actual object, bean type is [" +
                                "" + genealogyClass.getSimpleName() + "]", factorysError);
                    }
                }
                completeBean = initializeFactoryBean.doInitialize();
            }
            return (B) completeBean;
        }
    }

    /***
     * get a proxy factory. How to get the subclass implementation
     * @return proxy factory
     */
    public abstract ReusingProxyFactory getProxyFactory();

    /***
     * Get an object, According to class type
     * @param genealogyClass All class objects on the implementation
     *                       and inheritance chain
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @param <B> class type
     * @return Get a singleton object. If there are multiple objects,
     *         select one at random
     */
    @Override
    public <B> B getSingleBean(Class<B> genealogyClass, Map<String, Object> tempSource) {
        List<B> beans = getBean(genealogyClass, tempSource);
        return beans.isEmpty() ? null : beans.get(0);
    }

    /***
     * In order to create the factory and enjoy the freedom of object
     * creation, we can not only create the factory, but also pull the
     * object into the factory
     * @param genealogyClass All class objects on the implementation and inheritance chain
     * @param <B> class type
     * @return Returns an object that has been created
     */
    @Override
    public <B> B prototypeCreateBean(Class<B> genealogyClass) {
        return prototypeCreateBean(genealogyClass, null);
    }

    /***
     * In order to create the factory and enjoy the freedom of object
     * creation, we can not only create the factory, but also pull the
     * object into the factory
     * @param genealogyClass All class objects on the implementation and inheritance chain
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @param <B> class type
     * @return Returns an object that has been created
     */
    @Override
    public <B> B prototypeCreateBean(Class<B> genealogyClass, Map<String, Object> tempSource) {
        ReentrantLock lock = prototypeLocks.computeIfAbsent(genealogyClass, sc -> new ReentrantLock());
        lock.lock();
        try {
            BeanDefinitional<?> definitional = getDefinitional(genealogyClass);
            if(definitional == null){
                definitional = createDefinitional(genealogyClass, true);
            }
            return doCreateBean(definitional, tempSource);
        }catch (BeanFactorysException ex){
            throw new BeanFactorysException("An exception occurred while creating " +
                    "the atomic object, bean type is [" + genealogyClass.getName() + "]", ex);
        }finally {
            lock.unlock();
        }
    }

    /***
     * Create an object that will not be returned to
     * the cache because it existed before
     * @param genealogyClass All class objects on the implementation and inheritance chain
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @param <B> class type
     * @return concrete instance object created
     */
    @Override
    public <B> B createBean(Class<B> genealogyClass, Map<String, Object> tempSource) {

        try {
            //create definitional
            BeanDefinitional<B> definitional = createDefinitional(genealogyClass, false);
            final String beanName = definitional.getBeanName();
            if (!definitional.willInstanceCheckBean()) {
                if (log.isErrorEnabled()) {
                    log.error("The definition of the object determines " +
                            "that it is not eligible to be instantiated, " +
                            "name: {}", beanName);
                }
                throw new BeanFactorysException("is not eligible to be instantiated, name: [" + beanName + "]");
            }

            if (isCreating(genealogyClass)) {
                if (log.isErrorEnabled()) {
                    log.error("The object to be created is being created. " +
                                    "There may be circular dependencies. The object you want to get: {}",
                            genealogyClass.getSimpleName());
                }
                throw new BeanFactorysException("possible cyclic dependency, get bean: " + beanName);
            }

            //Intercept object loading and throw an exception
            if (interceptBean(definitional)) {
                throw new InterceptBeansException();
            }

            //application creation
            joinCreaing(genealogyClass);


            try {

                if (definitional.isLazy()){
                    try {
                        return (B) LazyFactory.lazyProxyBean(definitional, this);
                    }catch (Throwable ex){
                        if (log.isInfoEnabled()) {
                            log.info("Error occurred while creating lazy loading object, current " +
                                    "create bean is : {}", definitional.getBeanName());
                        }
                        throw new BeanFactorysException("Error occurred while creating lazy loading object, current " +
                                " create bean is : " + definitional.getBeanName(), ex);
                    }

                }


                return doCreateBean(definitional, tempSource);
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("An exception occurred during object creation, " +
                        "bean of creating is [" + definitional.getBeanName() + "]", ex);
            }finally {

                //creation complete
                finishCreating(genealogyClass);

                if (log.isDebugEnabled()) {
                    log.debug("The creation of the object is coming to " +
                            "an end, and the object is about to be registered of [{}]", definitional.getBeanName());
                }
            }

        } catch (Throwable ex){
            if (isCreating(genealogyClass)) {
                finishCreating(genealogyClass);
            }
            throw new BeanFactorysException("create bean error, bean type is " +
                    "[" + genealogyClass.getSimpleName() + "]", ex);
        }
    }

    /***
     * The interface that actually creates the object will not be
     * interfered by other external factors, and the object will not
     * be registered
     * @param definitional All class objects on the implementation and inheritance chain
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @param <B> class type
     * @return concrete instance object created
     */
    @Override
    public <B> B doCreateBean(BeanDefinitional<?> definitional, Map<String, Object> tempSource) {
        String beanName = definitional.getBeanName();
        Class<?> primordialClass = definitional.getClassWrapper().getPrimordialClass();

        //instance bean
        Object bean = instanceBean(definitional, tempSource);

        try {

            completeBean(bean, definitional);
        }catch (BeanFactorysException ex){
            throw new BeanFactorysException("complete bean has error, bean is [" + beanName + "]", ex);
        }

        boolean init = processorAfterInstance(bean, definitional);

        try {

            if (!init){
                if (log.isDebugEnabled()) {
                    log.debug("skip init, name: {}", beanName);
                }
                return (B) bean;
            }
            processorBeforeInitialize(bean, definitional);
            Object afterInitializeBean = initializeBean(bean, definitional);
            afterInitializeBean =  processorAfterInitialize(afterInitializeBean, definitional);

            bean = shapingBean(afterInitializeBean);
        }finally {

            //clear initialization queue
            initializeFactoryBeanMutes.remove(primordialClass);

            //finish
            end(bean);
        }

        return (B) bean;
    }

    protected boolean interceptBean(BeanDefinitional<?> definitional){
        Collection<BeanPostProcessor> beanProcessors = getBeanProcessors();
        for (BeanPostProcessor beanProcessor : beanProcessors) {
            if (beanProcessor instanceof BeanLifeCycleProcessor) {
                BeanLifeCycleProcessor cycleProcessor = (BeanLifeCycleProcessor) beanProcessor;
                try {
                    if (cycleProcessor.interceptBeanInstance(definitional, this)) {
                        if (log.isInfoEnabled()) {
                            log.info("The object processor intercepts the object " +
                                    "before it is loaded, intercept processor is {}, " +
                                    "load bean is {}", getProcessorName(cycleProcessor), definitional.getBeanName());
                        }
                        return true;
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("During bean processing, when instantiating bean preprocessing, " +
                                        "the processor has an exception and the instantiation is interrupted, " +
                                        "working processor: {}, creating bean: {}", BeanUtil.getPrimordialClass(cycleProcessor).getSimpleName(),
                                definitional.getBeanName());
                    }
                    throw new BeanFactorysException("before post instance error");
                }
            }
        }
        return false;
    }

    /***
     * Create an object identity definition
     * @param genealogyClass specific types
     * @param prototype Is it a prototype object
     * @param <B> class type
     * @return object identity definition
     */
    @Override
    public <B> BeanDefinitional<B> createDefinitional(Class<B> genealogyClass, boolean prototype) {
        if (definitionalCache.containsKey(genealogyClass)){
            return (BeanDefinitional<B>) definitionalCache.get(genealogyClass);
        }
        try {

            DefaultBeanDefinitional<B> definitional = new DefaultBeanDefinitional<>(genealogyClass, prototype);
            definitionalCache.put(genealogyClass, definitional);
            return definitional;
        }catch (RuntimeException ex){
            throw new BeanFactorysException("An exception occurred while creating the identity " +
                    "definition of the bean, bean type is [" + genealogyClass + "]", ex);
        }
    }

    /***
     * Register an identity definition, If it already exists, an exception will be thrown
     * @param definitional an identity definition
     */
    @Override
    public void registerDefinitional(@NonNull BeanDefinitional<?> definitional) {
        Class<?> primordialClass = definitional.getClassWrapper().getPrimordialClass();
        if (definitionalCache.containsKey(primordialClass)){
            if (log.isWarnEnabled()) {
                log.warn("The identity definition of this bean already exists." +
                        " You cannot register the same identity definition, " +
                        "Duplicate identity definition class: [{}]", definitional.getBeanName());
            }
            throw new BeanFactorysException("duplicate identity definition");
        }
        definitionalCache.put(primordialClass, definitional);
    }

    /***
     * Get factory processor
     * @return factory processor list
     */
    @Override
    public Collection<BeanFactoryProcessor> getBeanFactoryProcessors() {
        return factoryProcessors;
    }

    /***
     * Get bean processor
     * @return bean processor list
     */
    @Override
    public Collection<BeanPostProcessor> getBeanProcessors() {
        return beanPostProcessors;
    }

    protected void joinCreaing(Class<?> type){
        if (creatingQueue.contains(type)) {
            if (log.isWarnEnabled()) {
                log.warn("The element applying to join the creation " +
                        "queue already exists in the creation queue");
            }
            throw new BeanFactorysException("The element applying to join the " +
                    "creation queue already exists in the creation queue, type is [" + type + "]");
        }
        creatingQueue.add(type);
    }

    protected boolean isCreating(Class<?> type){
        return creatingQueue.contains(type);
    }

    protected void finishCreating(Class<?> type){
        creatingQueue.remove(type);
        if (log.isDebugEnabled()) {
            log.debug("finish create bean, type is [{}]", type.getSimpleName());
        }
    }

    /***
     * Object instantiation preprocessing
     * @param definitional object identity definition
     * @return bean
     */
    @Override
    public Object processorBeforeInstance(@NonNull BeanDefinitional<?> definitional) {
        Collection<BeanPostProcessor> beanProcessors = getBeanProcessors();
        Object chainBean = null;
        for (BeanPostProcessor beanProcessor : beanProcessors) {
            if (beanProcessor instanceof BeanLifeCycleProcessor) {
                BeanLifeCycleProcessor cycleProcessor = (BeanLifeCycleProcessor) beanProcessor;
                try {

                    chainBean = cycleProcessor.beforeBeanInstance(definitional, this, chainBean);
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("During bean processing, when instantiating bean preprocessing, " +
                                "the processor has an exception and the instantiation is interrupted, " +
                                "working processor: [{}], creating bean: [{}]", BeanUtil.getPrimordialClass(cycleProcessor).getSimpleName(),
                                definitional.getBeanName());
                    }
                    throw new BeanFactorysException("before post instance error");
                }
            }
        }
        return chainBean;
    }

    /***
     * Instantiate an object, excluding initialization
     * @param definitional object identity definition
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @return bean
     */
    @Override
    public Object instanceBean(BeanDefinitional<?> definitional, Map<String, Object> tempSource) {

        ConstructorWrapper<?> constructorWrapper;
        try {

             //get main constructor
             constructorWrapper = definitional.mainConstructor();
        } catch (NoSuchMethodException e) {
            if (isStaticBean(definitional)){
                return invokeGetInstanceBean(definitional, tempSource);
            }

            if (log.isErrorEnabled()) {
                log.error("The object constructor cannot be obtained successfully. " +
                        "Please expose a parameterless constructor or a constructor " +
                        "with annotations as @MainConstructor, fail bean object: [{}]", definitional.getBeanName());
            }
            throw new BeanFactorysException("error get constructor of [" + definitional.getBeanName() + "]");
        }

        Object bean;
        Object beforeInstanceBean = processorBeforeInstance(definitional);
        if (beforeInstanceBean != null){
            if (log.isDebugEnabled()) {
                log.debug("In the pre operation of instantiating an object, " +
                        "skip the instantiation stage");
            }
            bean = beforeInstanceBean;
        }else {

            //get constructor bean Definitional map
            final Map<ParameterWrapper, BeanDefinitional<?>> constructorDefinitionalMap
                    = definitional.instanceConstructorWrapper(constructorWrapper, this);

            //processing constructor parameters
            final Object[] constructorArgs = getConstructorArgs(constructorDefinitionalMap, tempSource, constructorWrapper);

            //instance
            bean = doInstanceBean(constructorArgs, constructorWrapper, definitional);

            invokeBeanPostConstructor(bean, definitional);
        }
        return bean;
    }


    public Object invokeGetInstanceBean(BeanDefinitional<?> definitional, Map<String, Object> tempSource){
        MethodWrapper methodWrapper = definitional.getClassWrapper().getSingleMethod("getInstance");
        if (methodWrapper == null){
            throw new BeanFactorysException("Unable to obtain the specified getInstance method and subsequently " +
                    "unable to instantiate the target: " + definitional.getBeanName());
        }
        Object[] args = tempSourceCastToArgs(methodWrapper, tempSource);
        prepareMethodParams(args, null, methodWrapper);
        return methodWrapper.invoke(null, args);
    }


    protected Object[] tempSourceCastToArgs(MethodWrapper methodWrapper, Map<String, Object> tempSource){
        Object[] args = new Object[methodWrapper.getParameterCount()];
        for (ParameterWrapper parameterWrapper : methodWrapper.getParameterArray()) {
             Object value = null;
             if (tempSource != null){
                 value = tempSource.get(parameterWrapper.getName());
             }
             args[parameterWrapper.getIndex()] = value;
        }
        return args;
    }

    protected boolean isStaticBean(BeanDefinitional<?> definitional){
        ClassWrapper<?> classWrapper = definitional.getClassWrapper();
        MethodWrapper methodWrapper = classWrapper.getSingleMethod("getInstance");
        if (methodWrapper != null){
            Method method = methodWrapper.get();
            if (Modifier.isStatic(method.getModifiers())){
                return true;
            }
        }
        return false;
    }

    public Object doInstanceBean(Object[] constructorArgs, ConstructorWrapper cw, BeanDefinitional<?> definitional){
        if (definitional.requiredAgent() && definitional.getProxyType() == ProxyType.INSTANCE_PROXY) {
            ReusingProxyFactory proxyFactory = getProxyFactory();
            Class[] parameterTypes = cw.getParameterTypes();
            if (log.isDebugEnabled()) {
                log.debug("proxy object: [{}]", definitional.getBeanName());
            }
            return proxyFactory.proxy(definitional.getPrimordialClass(), parameterTypes, constructorArgs, createAgentLayer(definitional));
        }else {
            return cw.newInstance(constructorArgs);
        }
    }

    protected AgentLayer createAgentLayer(BeanDefinitional<?> definitional){
        Class<? extends AgentLayer> layer = definitional.getAgentLayerType();
        if (layer == null){
            throw new BeanFactorysException("trying to proxy an object, making sure that the " +
                    "object proxy processor type could not be obtained, bean is [" + definitional.getBeanName() + "]");
        }
        AgentLayer agentLayer;
        if(BeanProxy.class.equals(layer)){
            agentLayer = new BeanProxy(this, definitional);
        }else {
            agentLayer = ReflexUtils.instance(layer);
        }

        definitional.setAgentLayer(agentLayer);
        return agentLayer;
    }

    public Object[] getConstructorArgs(Map<ParameterWrapper, BeanDefinitional<?>> constructorDefinitionalMap,
                                        Map<String, Object> tempSource,
                                        ConstructorWrapper<?> constructorWrapper){
        Object[] args = new Object[constructorWrapper.getParamCount()];
        List<Integer> handlerIndex = new ArrayList<>();
        if (tempSource != null){
            tempSource.forEach((name, value) ->{
                ParameterWrapper param = constructorWrapper.queryParam(name);
                if (param != null){
                    int index = param.getIndex();
                    args[index] = value;
                    handlerIndex.add(index);
                    if (log.isDebugEnabled()) {
                        log.debug("Injection parameters from tempSource, index: [{}]", index);
                    }
                }
            });
        }

        for (ParameterWrapper wrapper : constructorDefinitionalMap.keySet()) {
            BeanDefinitional<?> definitional = constructorDefinitionalMap.get(wrapper);
            int index = wrapper.getIndex();
            if (!handlerIndex.contains(index)){
                Object paramValue = null;
                for (BeanFactoryProcessor beanFactoryProcessor : getBeanFactoryProcessors()) {
                    if (beanFactoryProcessor instanceof BeanMethodHandler){
                        BeanMethodHandler methodHandler = (BeanMethodHandler) beanFactoryProcessor;

                        //If the process can process the parameter
                        if (methodHandler.support(constructorWrapper, wrapper, null)) {
                            try {

                                paramValue = methodHandler.structure(constructorWrapper, wrapper, this, paramValue);
                            }catch (RuntimeException ex){
                                if (log.isWarnEnabled()) {
                                    log.warn("When the factory object method processor handles " +
                                            "exceptions, it needs to digest them instead of " +
                                            "flowing out. The flowing out exceptions will lead " +
                                            "to the execution failure of the whole process");
                                }
                                throw new BeanFactorysException("When the factory object method processor handles " +
                                        "exceptions, it needs to digest them instead of " +
                                        "flowing out. The flowing out exceptions will lead " +
                                        "to the execution failure of the whole process: [" + getProcessorName(beanFactoryProcessor) + "]" +
                                        ", treatment constructor: [" + constructorWrapper.getConstructor() + "]", ex);
                            }
                        }
                    }
                }
                try {

                    args[index] = paramValue;
                    if (log.isDebugEnabled()) {
                        log.debug("Injection parameters from get bean, index: [{}]", index);
                    }
                }catch (BeanFactorysException bfe){
                    if (log.isErrorEnabled()) {
                        log.error("An exception occurred while creating an object while " +
                                "seeking constructor parameters, Main object：[{}]", definitional.getBeanName());
                    }
                    throw new BeanFactorysException("An exception occurred while creating an object while" +
                            "seeking constructor parameters, Main object：[" + definitional.getBeanName() + "]", bfe);
                }
            }
        }

        return args;
    }

    /***
     * Execute constructor post-processing
     * @param bean bean
     * @param definitional object identity definition
     */
    @Override
    public void invokeBeanPostConstructor(Object bean, BeanDefinitional<?> definitional) {
        MethodWrapper methodWrapper = definitional.getPostConstructorMethod();
        if(methodWrapper != null){
            invokeBeanMethod(bean, methodWrapper);
            if (log.isDebugEnabled()) {
                log.debug("After executing the constructor post method of the object");
            }
        }
    }

    /***
     * instantiation post-processing
     * @param dryBean uninitialized object
     * @param definitional object identity definition
     * @return true if properties should be set on the bean;
     *          false if property population should be skipped.
     *          Normal implementations should return true. Returning
     *          false will also prevent any subsequent InstantiationAwareBeanPostProcessor
     *          instances being invoked on this bean instance.
     */
    @Override
    public boolean processorAfterInstance(Object dryBean, BeanDefinitional<?> definitional) {
        Collection<BeanPostProcessor> beanProcessors = getBeanProcessors();
        for (BeanPostProcessor beanProcessor : beanProcessors) {
            if (beanProcessor instanceof BeanLifeCycleProcessor) {
                BeanLifeCycleProcessor cycleProcessor = (BeanLifeCycleProcessor) beanProcessor;
                try {

                    boolean continueInitialize = cycleProcessor.afterBeanInstance(dryBean, definitional, this);
                    if (!continueInitialize){
                        if (log.isDebugEnabled()) {
                            log.debug("The bean processor blocks the initialization " +
                                    "processing of the bean: [{}], The blocked processor is the blocked bean: [{}]",
                                    getProcessorName(cycleProcessor), definitional.getBeanName());
                        }
                        return false;
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("During bean processing, when instantiating bean preprocessing, " +
                                        "the processor has an exception and the instantiation is interrupted, " +
                                        "working processor: [{}], creating bean: [{}]", getProcessorName(cycleProcessor),
                                definitional.getBeanName());
                    }
                    throw new BeanFactorysException("before post instance error");
                }
            }
        }
        return true;
    }

    /***
     * Instantiation completion processing object
     * @param bean uninitialized object
     * @param definitional object identity definition
     */
    @Override
    public void completeBean(Object bean, BeanDefinitional<?> definitional) {
        MethodWrapper initMethod = definitional.getInitMethod();
        if (initMethod != null){
            try {

                invokeBeanMethod(bean, initMethod);
            }catch (BeanFactorysException ex){
                throw new BeanFactorysException("After the instantiation phase of the bean is completed, " +
                        "an exception occurs when executing the custom initialization " +
                        "method, error bean: [" + bean + "]", ex);
            }
            if (log.isDebugEnabled()) {
                log.debug("After executing the initialization method of the object");
            }
        }

        if (bean instanceof InstanceBean) {
            InstanceBean instanceBean = (InstanceBean) bean;
            try {

                instanceBean.instanceComplete(this);
            }catch (Throwable ex){
                if (log.isWarnEnabled()) {
                    log.warn("After the object instantiation is finished, " +
                            "the instantiation of the post completion completion " +
                            "method is called an exception, and the abort process " +
                            "is aborted, InstanceBean: [{}]", getProcessorName(bean));
                }
                throw new BeanFactorysException("After the object instantiation is finished, " +
                        "the instantiation of the post completion completion " +
                        "method is called an exception, and the abort process " +
                        "is aborted, InstanceBean: [" + getProcessorName(bean) + "]", ex);
            }
        }
    }

    /***
     * Initialize preprocessing
     * @param dryBean uninitialized object
     * @param definitional object identity definition
     * @return the bean instance to use, either the original or a wrapped one; if null, no subsequent BeanPostProcessors will be invoked
     */
    @Override
    public Object processorBeforeInitialize(Object dryBean, BeanDefinitional<?> definitional) {
        Collection<BeanPostProcessor> beanProcessors = getBeanProcessors();
        for (BeanPostProcessor beanProcessor : beanProcessors) {
            if (beanProcessor instanceof BeanLifeCycleProcessor) {
                BeanLifeCycleProcessor cycleProcessor = (BeanLifeCycleProcessor) beanProcessor;
                try {

                    Object postBean = cycleProcessor.beforeBeanInitialize(dryBean, definitional, this);
                    if (postBean == null){
                        if (log.isDebugEnabled()) {
                            log.debug("The bean processor blocks the initialization " +
                                            "processing of the bean: [{}], The blocked processor is the blocked bean: [{}]",
                                    getProcessorName(cycleProcessor), definitional.getBeanName());
                        }
                        break;
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("During bean processing, when instantiating bean preprocessing, " +
                                        "the processor has an exception and the instantiation is interrupted, " +
                                        "working processor: [{}], creating bean: [{}]", getProcessorName(cycleProcessor),
                                definitional.getBeanName());
                    }
                    throw new BeanFactorysException("before post instance error");
                }
            }
        }
        return dryBean;
    }

    /***
     * gets the condition engine supported by the current factory
     * @return condition engine
     */
    @Override
    public ConditionalEngine getConditionalEngine() {
        if (conditionalEngine == null){
            conditionalEngine = ConditionalHodler.obtainEngine(list -> {
                list.remove(DefaultConditionalOnClass.class);
                list.add(BeanFactoryConditionalOnClass.class);
            }, "beanFactory");
        }
        return conditionalEngine;
    }

    /**
     * Performs a task and is constrained by the condition processor
     * Only when the conditions are met will it be implemented
     * @param element conditionally mounted objects
     * @param source environmental data for condition judgment
     * @param callable task
     * @param <C> result type
     * @return task result
     */
    @Override
    public <C> C callConditions(AnnotatedElement element, Object source, Callable<C> callable) {
        ConditionalEngine engine = getConditionalEngine();
        try {
            if (engine.resolveCondition(element, source)) {
                try {

                    return callable.call();
                }catch (Throwable re){
                    throw new BeanFactorysException("run task in [callConditions] of throwable", re);
                }
            }
        }catch (Throwable e){
            throw new BeanFactorysException("[callConditions] ill of throwable of:" + e.getMessage(), e);
        }
        return null;
    }

    /***
     * Performs a task and is constrained by the condition processor
     * Only when the conditions are met will it be implemented
     * @param element conditionally mounted objects
     * @param source environmental data for condition judgment
     * @param runnable task
     */
    @Override
    public void runConditions(AnnotatedElement element, Object source, Runnable runnable) {
        ConditionalEngine engine = getConditionalEngine();
        try {
            if (engine.resolveCondition(element, source)) {
                try {
                    runnable.run();
                }catch (Throwable re){
                    throw new BeanFactorysException("run task in [runConditions] of throwable", re);
                }
            }
        }catch (Throwable e){
            throw new BeanFactorysException("[runConditions] ill of throwable of:" + e.getMessage(), e);
        }
    }

    /***
     * Initialize bean
     * @param bean uninitialized object
     * @param definitional object identity definition
     * @return bean
     */
    @Override
    @SuppressWarnings("all")
    public Object initializeBean(Object bean, BeanDefinitional<?> definitional) {
        Class<?> primordialClass = definitional.getClassWrapper().getPrimordialClass();

        if (definitional.isLazy()) {
            return initializeBean0(bean, definitional);
        }else {
            initializeFactoryBeanMutes.put(primordialClass, new InitializeFactoryBean() {
                @Override
                public Object doInitialize() {
                    return initializeBean0(bean, definitional);
                }
            });
            return getSingleBean(primordialClass);
        }
    }

    @SuppressWarnings("all")
    public Object initializeBean0(Object bean, BeanDefinitional<?> definitional){

        Collection<BeanFactoryProcessor> beanFactoryProcessors = getBeanFactoryProcessors();

        //Scan own attributes
        //wriedClassFields(definitional.getClassWrapper(), bean);

        //Scan all its parent classes to realize attribute injection
        Set<Class<?>> superClasses = definitional.getSuperClasses();
        for (Class<?> superClass : superClasses) {
            if (!superClass.equals(Object.class)){
                ClassWrapper<?> classWrapper = classWrapperCache.computeIfAbsent(superClass, sc -> {
                    return ClassWrapper.get(sc);
                });

                wriedClassFields(classWrapper, bean);
            }
        }
        autoWriedBean(bean);
        if (definitional.requiredAgent() && definitional.getProxyType() == ProxyType.INITIALIZATION){
            bean = initializeProxyBean(bean, definitional);
        }
        return bean;
    }

    protected Object initializeProxyBean(Object bean, BeanDefinitional<?> definitional){
        ApplyBeanProxy applyBeanProxy = new ApplyBeanProxy(this, definitional);
        return ApplyProxyFactory.proxy(bean, applyBeanProxy);
    }

    /***
     * fill an object
     * @param bean bean
     */
    @Override
    public void autoWriedBean(Object bean){
        BeanDefinitional<?> definitional = getDefinitional(bean);
        if (definitional == null){
            definitional = createDefinitional(BeanUtil.getPrimordialClass(bean), false);
        }
        wriedClassFields(definitional.getClassWrapper(), bean);
    }

    protected void wriedClassFields(ClassWrapper<?> wrapper, Object bean){

        Collection<FieldWrapper> fields = wrapper.getFields();
        for (FieldWrapper field : fields) {
            runConditions(field.getField(), null, () -> {
                doWiredClassField(field, bean);
            });
        }
    }

    protected void doWiredClassField(FieldWrapper field, Object bean){
        Collection<BeanFactoryProcessor> beanFactoryProcessors = getBeanFactoryProcessors();
        try {
            for (BeanFactoryProcessor beanFactoryProcessor : beanFactoryProcessors) {
                        /*
                            The field is scanned by the factory processor,
                            and then the injection is realized
                         */
                if (beanFactoryProcessor instanceof BeanInitializationHandler) {
                    BeanInitializationHandler initializationHandler = (BeanInitializationHandler) beanFactoryProcessor;
                    if (initializationHandler.support(field, this, bean)) {
                        initializationHandler.doHandler(field, this, bean);
                    }
                }
            }
        }catch (BeanFactorysException ex){
            throw new BeanFactorysException("An exception occurred while injecting object field properties, " +
                    "Objects being processed: [" + bean + "], Fields being injected: [" + field.getName() + "], " +
                    "", ex);
        }
    }

    protected void end(Object bean){
        for (BeanPostProcessor beanProcessor : getBeanProcessors()) {
            try {

                beanProcessor.aboutFactory(bean, this);
            }catch (RuntimeException ex){
                if (log.isErrorEnabled()) {
                    log.error("The creation of bean object has been " +
                            "completed, and an exception occurred in unset " +
                            "processing， error processor is [{}], error bean is [{}]",
                            getProcessorName(beanProcessor), getBeanName(bean));
                }
                throw new BeanFactorysException("The creation of bean object has been " +
                        "completed, and an exception occurred in unset " +
                        "processing， error processor is " + getProcessorName(beanProcessor) + ", " +
                        "error bean is [" + getBeanName(bean) + "]", ex);
            }
        }
    }

    /***
     * Initialize post-processing
     * @param completeBean Initialization completed object
     * @param definitional object identity definition
     * @return bean
     */
    @Override
    public Object processorAfterInitialize(Object completeBean, BeanDefinitional<?> definitional) {
        Collection<BeanPostProcessor> beanProcessors = getBeanProcessors();
        for (BeanPostProcessor beanProcessor : beanProcessors) {
            if (beanProcessor instanceof BeanLifeCycleProcessor) {
                BeanLifeCycleProcessor cycleProcessor = (BeanLifeCycleProcessor) beanProcessor;
                try {

                    Object postBean = cycleProcessor.afterBeanInitialize(completeBean, definitional, this);
                    if (postBean == null){
                        if (log.isDebugEnabled()) {
                            log.debug("The bean processor blocks the initialization " +
                                            "processing of the bean: [{}], The blocked processor is the blocked bean: [{}]",
                                    getProcessorName(cycleProcessor), definitional.getBeanName());
                        }
                        break;
                    }
                } catch (Throwable e) {
                    if (log.isErrorEnabled()) {
                        log.error("During bean processing, when instantiating bean preprocessing, " +
                                        "the processor has an exception and the instantiation is interrupted, " +
                                        "working processor: [{}], creating bean: [{}]", getProcessorName(cycleProcessor),
                                definitional.getBeanName());
                    }
                    throw new BeanFactorysException("before post instance error");
                }
            }
        }
        return completeBean;
    }

    /***
     * Initialization completion processing
     * @param bean bean
     * @return bean
     */
    @Override
    public Object shapingBean(Object bean) {
        if (bean != null){
            if (bean instanceof InitializeBean){
                InitializeBean initializeBean = (InitializeBean) bean;
                try {
                    initializeBean.initializeComplete(this);
                }catch (Throwable ex){
                    if (log.isWarnEnabled()) {
                        log.warn("After the object Initialize is finished, " +
                                "the instantiation of the post completion completion " +
                                "method is called an exception, and the abort process " +
                                "is aborted, initializeBean: [{}]", getProcessorName(bean));
                    }
                    throw new BeanFactorysException(ex);
                }
            }

            if(bean instanceof FactoryBean){
                FactoryBean factoryBean = (FactoryBean) bean;
                try {

                    return factoryBean.getObject();
                }catch (Throwable e){
                    if (log.isErrorEnabled()) {
                        log.error("An exception occurred while calling the get " +
                                "real object method of the factory object, Factory object: [{}]",
                                getProcessorName(bean));
                    }
                }
            }
        }
        BeanDefinitional<?> definitional = getDefinitional(bean);
        MethodWrapper completeMethod = definitional.getCompleteMethod();
        if (completeMethod != null){
            invokeBeanMethod(bean, completeMethod);
        }
        return bean;
    }


    /***
     * Registration object itself and its mapping relationship
     * @param bean bean
     */
    @Override
    public void registerBean(Object bean) {
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        BeanDefinitional<?> definitional = getDefinitional(primordialClass);
        if (definitional == null){
            definitional = createDefinitional(primordialClass, false);
            if (log.isDebugEnabled()) {
                log.debug("In the process of registering an object, " +
                        "if it is detected that the object is not created " +
                        "and passed, re register the identity definition " +
                        "information, register bean: [{}]", definitional.getBeanName());
            }
        }

        if (instanceMutes.containsKey(primordialClass)){
            if (log.isErrorEnabled()) {
                log.error("The bean to be registered already exists");
            }
            throw new BeanFactorysException("register bean: [" + definitional.getBeanName() + "] already exists");
        }

        if (classMapping.containsKey(primordialClass)){
            if (log.isWarnEnabled()) {
                log.warn("The bean to be registered already " +
                        "has a mapping relationship, target bean: [{}]", definitional.getBeanName());
            }
            return;
        }

        List<Class<?>> ancestorsClasses = getAncestorsClasses(definitional);
        for (Class<?> ancestorsClass : ancestorsClasses) {
            List<Class<?>> mappingList = classMapping.computeIfAbsent(ancestorsClass, ac -> {
                return new ArrayList<>();
            });
            mappingList.add(primordialClass);
        }
        instanceMutes.put(primordialClass, bean);

        /*
            If the registered objects are function objects,
            they will be additionally registered
         */
        if (bean instanceof BeanFactoryProcessor){
            registerBeanFactoryProcessor((BeanFactoryProcessor) bean);
        }

        if (bean instanceof BeanPostProcessor){
            registerBeanLifeCycleProcessor((BeanPostProcessor) bean);
        }
    }

    protected List<Class<?>> getAncestorsClasses(BeanDefinitional<?> definitional){
        List<Class<?>> result = new ArrayList<>();
        Set<Class<?>> superClasses = definitional.getSuperClasses();
        Set<Class<?>> interfaceWrappers = definitional.getInterfaceWrappers();
        result.addAll(superClasses);
        result.addAll(interfaceWrappers);
        result.add(definitional.getClassWrapper().getPrimordialClass());
        return result;
    }


    /***
     * prepare the parameters required for the execution of a method
     * @param args Source parameters, and subsequent processors will
     *             perform different Jia based on metadata
     * @param bean as bean
     * @param mw method
     * @return args
     */
    @Override
    public Object[] prepareMethodParams(Object[] args, Object bean, MethodWrapper mw) {
        if (args == null){
            args = new Object[mw.getParameterCount()];
        }else {
            if (args.length != mw.getParameterCount()){
                throw new BeanFactorysException("When preparing the parameters of the object " +
                        "method, it is found that the length of the passed parameter array does " +
                        "not meet the standard. Please check the composition of the passed parameter, " +
                        "method: [" + mw.getName() + "], bean is [" + getBeanName(bean) + "]");
            }
        }
        for (ParameterWrapper parameterWrapper : mw.getParameterWrappersSet()) {
            /*
                Traverse the parameter list of the method to be executed
                and find all processors that process the parameter
             */
            Object paramValue = args[parameterWrapper.getIndex()];
            for (BeanFactoryProcessor beanFactoryProcessor : getBeanFactoryProcessors()) {
                if (beanFactoryProcessor instanceof BeanMethodHandler){
                    BeanMethodHandler methodHandler = (BeanMethodHandler) beanFactoryProcessor;

                    //If the process can process the parameter
                    if (methodHandler.support(mw, parameterWrapper, bean)) {
                        try {

                            paramValue = methodHandler.handler(mw, parameterWrapper, bean, this, paramValue);
                        }catch (RuntimeException ex){
                            if (log.isWarnEnabled()) {
                                log.warn("When the factory object method processor handles " +
                                        "exceptions, it needs to digest them instead of " +
                                        "flowing out. The flowing out exceptions will lead " +
                                        "to the execution failure of the whole process");
                            }
                            throw new BeanFactorysException("When the factory object method processor handles " +
                                    "exceptions, it needs to digest them instead of " +
                                    "flowing out. The flowing out exceptions will lead " +
                                    "to the execution failure of the whole process: [" + getProcessorName(beanFactoryProcessor) + "]" +
                                    ", treatment method: [" + mw.getName() + "]", ex);
                        }
                    }
                }
            }
            args[parameterWrapper.getIndex()] = paramValue;
        }
        return args;
    }

    /***
     * Execution object method
     * @param bean bean
     * @param methodWrapper methodWrapper
     * @return result
     */
    @Override
    public Object invokeBeanMethod(Object bean, MethodWrapper methodWrapper) {

        Object[] args = new Object[methodWrapper.getParameterCount()];
        args = prepareMethodParams(args, bean, methodWrapper);
        Object result;
        try {

            result = methodWrapper.invoke(bean, args);
        }catch (RuntimeException e){
            if (log.isWarnEnabled()) {
                log.warn("An exception occurred while executing the bean method");
            }
            throw new BeanFactorysException("invoke bean method error, bean type: [" +
                    BeanUtil.getPrimordialClass(bean).getSimpleName() + "], error method is [" +
                    methodWrapper.getName() + "]", e);
        }
        return afterInvokeMethod(bean, result, methodWrapper);
    }


    /***
     * result value of processing method
     * @param bean as bean
     * @param value return value
     * @param mw target method
     * @return return value after machining
     */
    @Override
    public Object afterInvokeMethod(Object bean, Object value, MethodWrapper mw){
        for (BeanFactoryProcessor processor : getBeanFactoryProcessors()) {
            if (processor instanceof BeanMethodHandler) {
                BeanMethodHandler bmh = (BeanMethodHandler) processor;
                try {
                    if (bmh.supportReturnProcessor(mw, value)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Method processor successfully hit method post-processing, " +
                                    "processor:[{}], hit method:[{}]", getProcessorName(processor), mw.getName());
                        }
                        value = bmh.processor(mw, value, bean, this);
                    }
                }catch (Throwable e){
                    throw new BeanFactorysException("An exception occurs during post-processing of the " +
                            "processing method. The processor of the exception: [" + getProcessorName(processor) + "], " +
                            "treatment method: [" + mw.getName() + "]", e);
                }
            }
        }
        return value;
    }


    /***
     * Does this type of object exist
     * @param type type
     * @return is contain
     */
    @Override
    public boolean containBean(Class<?> type) {
        if (!classMapping.containsKey(type)) {
            return false;
        }
        return !classMapping.get(type).isEmpty();
    }

    /***
     * get definitional
     * @param bean bean
     * @return object identity definition
     */
    @Override
    public BeanDefinitional<?> getDefinitional(Object bean) {
        return getDefinitional(BeanUtil.getPrimordialClass(bean));
    }

    /***
     * get definitional
     * @param beanClass beanClass
     * @return object identity definition
     */
    @Override
    public BeanDefinitional<?> getDefinitional(Class<?> beanClass) {
        return definitionalCache.get(beanClass);
    }

    /***
     * Delete the existence of the object in the
     * factory and clear its mapping relationship
     * @param type type
     */
    @Override
    @SuppressWarnings("all")
    public void removes(Class<?> type) {
        BeanDefinitional<?> definitional = definitionalCache.get(type);
        if (definitional == null){
            if (log.isWarnEnabled()) {
                log.warn("The object to be deleted has a mapping relationship, " +
                        "but there is no identity definition, target: [{}]", type.getSimpleName());
            }
            throw new BeanFactorysException("error for not save definition");
        }
        List<Class<?>> ancestorsClasses = getAncestorsClasses(definitional);
        for (Class<?> ancestorsClass : ancestorsClasses) {
            List<Class<?>> classes = classMapping.get(ancestorsClass);
            if (classes != null){
                classes.removeIf(new Predicate<Class<?>>() {
                    @Override
                    public boolean test(Class<?> element) {
                        return element.equals(type);
                    }
                });
            }
        }
        Object removeBean = instanceMutes.remove(type);
        if (removeBean == null){
            if (log.isWarnEnabled()) {
                log.warn("An empty object was deleted ?");
            }
        }
        definitionalCache.remove(type);
        classMapping.remove(type);
    }

    /***
     * Destroy an object
     * @param bean bean
     */
    @Override
    public void destroyBean(Object bean) {
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        if (containBean(primordialClass)) {
            BeanDefinitional<?> definitional = getDefinitional(primordialClass);
            MethodWrapper destroyMethod = definitional.getDestroyMethod();
            try {
                if (destroyMethod != null){
                    try {

                        invokeBeanMethod(bean, destroyMethod);
                    }catch (BeanFactorysException ex){
                        throw new BeanFactorysException("In the process of destroying a bean, " +
                                "an exception occurred in the destruction method of the bean, " +
                                "but the bean has been forcibly removed, error bean: [" + bean + "]", ex);
                    }
                }
            }finally {
                removes(primordialClass);
            }
            if (log.isDebugEnabled()) {
                log.debug("An object was successfully destroyed, type is [{}]", primordialClass);
            }
            return;
        }
        throw new BeanFactorysException("This type of object does not exist " +
                "inside the factory, let alone destroy it, type: [" + primordialClass + "]");
    }

    /***
     * clear all caches
     */
    @Override
    public void clearAll() {
        for (Field field : ReflexHandler.getAccessibleFields(this, true)) {
            Class<?> type = field.getType();
            if (Map.class.isAssignableFrom(type)){
                Map<?, ?> map = (Map<?, ?>) ReflexUtils.getValue(field, this);
                if (map != null){
                    map.clear();
                }
            }else if (Collection.class.isAssignableFrom(type)){
                Collection<?> collection = (Collection<?>) ReflexUtils.getValue(field, this);
                if(collection != null){
                    collection.clear();
                }
            }
        }
    }

    /***
     * returns all created instances
     * @return all created instances
     */
    @Override
    public Map<Class<?>, Object> getMutes() {
        return instanceMutes;
    }

    /***
     * Register factory processor
     * @param processor processor
     */
    @Override
    public void registerBeanFactoryProcessor(@NonNull BeanFactoryProcessor processor) {
        factoryProcessors.add(processor);
    }

    /***
     * Register bean processor
     * @param processor processor
     */
    @Override
    public void registerBeanLifeCycleProcessor(@NonNull BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    @Override
    public int size() {
        return instanceMutes.size();
    }

    protected String getProcessorName(Object processor){
        if (processor != null){
            return BeanUtil.getPrimordialClass(processor).getSimpleName();
        }
        return "null";
    }

    protected String getBeanName(Object bean){
        if (bean == null){
            return "null bean";
        }
        return NameUtil.getName(bean);
    }

    @Override
    public String toString() {
        if (flushString){
            System.out.println("~~~");
            System.out.println(" \\ \\");
            System.out.println("   \\ \\");
            System.out.println("    | |  ");
            System.out.println("    ****");
            System.out.println("    |  |            _");
            System.out.println("    |  |           | |");
            System.out.println("    |  |**************|");
            System.out.println("    |  |              |");
            System.out.println("    |  | []      []   |_");
            System.out.println("    |  |     ___      | | [bean] [bean] ...");
            System.out.println("    |  |    | R |     *=================>>>");
            System.out.println("*************************************************");
            System.out.println("[ " + getClass().getSimpleName() + " ]");
        }
        return getClass().getSimpleName() + "{bean(" + size() + ")}@" + hashCode();
    }
}

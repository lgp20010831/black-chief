package com.black.core.factory;

import com.black.core.builder.Col;
import com.black.core.spring.instance.*;
import com.black.utils.ReflexHandler;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Log4j2
public class DefaultInstanceSingtonFactory extends AbstractSingtonFactory<Class<?>, Object> implements InstanceSingtonFactory,
        BeanFactoryAware {

    /** 打印下耗时情况 */
    public boolean printInvokeTime = true;

    /** spring bean factory */
    private DefaultListableBeanFactory defaultListableBeanFactory;

    /***
     * 正在创建的实例对象,将会存贮一个标志在此队列中
     * 方便在中止一个实例生成时, 或者删除一个实例时
     * 可以快速的执行
     */
    private final Set<Class<?>> creatingQueue = ConcurrentHashMap.newKeySet();

    /***
     * 最终的实例缓存
     * 实例对象标准 class --> 最终实例对象
     * 也是工厂内部缓存实例重要部位
     */
    private final Map<Class<?>, Object> instanceMutes;

    /***
     * 早期的缓存, 在bean 还没有初始化的时候,将会被存到该缓存中
     * 以 实例对象标准 class --> 仅仅实例化完成,未初始化的对象
     */
    private final Map<Class<?>, Object> earlyInstanceMutes = new ConcurrentHashMap<>(16);

    /**
     * 初始化方法的缓存,如果在获取实例的过程中,仅仅完成了实例化
     * 以后, 其他线程来获取该资源, 该缓存正可以让其他线程帮助实现实例的
     * 初始化, 以 实例对象标准 class --> 初始化方法存贮
     */
    private final Map<Class<?>, InitializeInstance> initializeMutes = new ConcurrentHashMap<>(16);

    /** 工厂依赖于实例元素的加工工厂 */
    private final InstanceWrapperFactory instanceElementFactory;

    public DefaultInstanceSingtonFactory(InstanceWrapperFactory instanceElementFactory) {
        this.instanceElementFactory = instanceElementFactory;
        instanceMutes = new ConcurrentHashMap<>(16);
        parent = new ClassMappingFactory();
        registerInstance(DefaultInstanceSingtonFactory.class, this);
    }

    @Override
    public <T> T getInstance(Class<T> instanceClass) {
        return obtainSington(instanceClass);
    }


    /**
     * 获取一个实例的对象, 传递的 class 不能为空
     * 如果工厂内部存在该 class 类型的实例(判断的条件是
     * 工厂内部存在跟此 class 有直接关联的对象), 直接返回
     * 否则会进行创建, 通过注解选择合适的构造器, 并将构造方法
     * 的参数通过 getInstance 来获取
     * @param instanceClass 实例 class 对象
     * @param <T> 类型
     * @return 实例对象
     */
    public <T> T obtainSington(Class<T> instanceClass) {
        long startTime = System.currentTimeMillis();
        try {
            List<? extends T> instanceMutes = getInstanceMutes(instanceClass);
            return instanceMutes.isEmpty() ? null : instanceMutes.get(0);
        }finally {
            if (printInvokeTime){
                if (log.isInfoEnabled()) {
                    log.info("getInstance: {}, take time: {}ms, cache size:{}", instanceClass.getSimpleName(),
                            (System.currentTimeMillis() - startTime), ((ClassMappingFactory)getParent()).getSource().size());
                }
            }
        }
    }

    /***
     * 获取这个类型下的所有可能存在的类型
     * @param condition 条件 class 对象
     * @param <T> 指定类型
     * @return 返回结果, 可能为空
     */
    public <T> List<? extends T> getInstanceMutes(Class<T> condition) {
        Collection<Class<?>> mappingKeys = ((ClassMappingFactory)getParent()).getMappingKeys(condition);
        if (mappingKeys.isEmpty()){
            if (condition.isInterface() || condition.isEnum() || Modifier.isAbstract(condition.getModifiers())){
                return new ArrayList<>();
            }
            List<Class<? extends T>> sonList;
            List<? extends T> resultInstanceMute = null;
            if ((sonList = SingletonClazzTree.obtainSuperClazz(condition, earlyInstanceMutes.keySet())).isEmpty()){

                if ((sonList = SingletonClazzTree.obtainSuperClazz(condition, initializeMutes.keySet())).isEmpty()){
                    try {
                        return resultInstanceMute = (List<? extends T>) Col.as(obtainInstance(instanceElementFactory.get(condition)));
                    }finally {
                        if (resultInstanceMute != null && !resultInstanceMute.isEmpty()){
                            if (!registerInstance(condition, resultInstanceMute.get(0))) {
                                if (log.isDebugEnabled()) {
                                    log.debug("创建结束的对象, 注册失败, condition:{}", condition);
                                }
                            }
                        }
                    }
                }
                return (List<? extends T>) sonList.stream().map(k -> initializeMutes.get(k).initialize()).collect(Collectors.toList());
            }
            return (List<? extends T>) sonList.stream()
                    .map(s -> initializeInstance(earlyInstanceMutes.get(s), s)).collect(Collectors.toList());
        }
        return (List<? extends T>) mappingKeys.stream().map(this::get).collect(Collectors.toList());
    }

    protected <T> T get(Class<? extends T> target){
        return (T) instanceMutes.get(target);
    }

    /**
     * 直接向工厂内部缓存注册一个实例对象
     * 注册的 key 和 value 等于传递的参数
     * 返回值只是判断了他有没有跟他存在继承关系的
     * 实例, 返回 true 表示存在, false 表示不存在
     * 均不能为空
     * @param instanceClass 实例 class 对象
     * @param instance 实例对象
     * @return 返回判断结果
     */
    public <K, V extends K> boolean registerInstance(Class<K> instanceClass, V instance) {
        if (instance == null || instanceClass == null){
            throw new RuntimeException("不允许向工厂中注册空对象, class:" + instanceClass);
        }
        if (instanceMutes.containsKey(instanceClass)){
            return false;
        }else {
            ((ClassMappingFactory)getParent()).registerMapping(instanceClass, instance);
            instanceMutes.put(instanceClass, instance);
        }
        return true;
    }


    /**
     * 获取一个实例话对象, 作为{@link InstanceFactory#getInstance(Class)}
     * 的一个重要的步骤存在
     * @param element 实例对象的处理封装类
     * @param constructorArgMap 构造器参数 map
     * @param <T> 实例 class 类型
     * @return 返回实例对象
     */
    public <T> T obtainInstance(InstanceElement<T> element, Map<Class<?>, Object> constructorArgMap) {

        final Class<?> elementClass = element.instanceClass();
        estimateLoop(elementClass);
        T instance;
        try {
            //实例化完成
            instance = instance(element, constructorArgMap);
            if (!creatingQueue.contains(elementClass)){
                return instance;
            }
            //执行构造方法后的方法
            invokePostConstr(instance);
            //存入二级缓存中
            earlyInstanceMutes.put(elementClass, instance);
            //初始化实例
            initializeInstance(instance, elementClass);
        }finally {
            creatingQueue.remove(elementClass);
        }
        return instance;
    }


    private void invokePostConstr(Object instance){
        ReflexHandler.getAccessibleMethods(instance)
                .stream().filter(m -> AnnotationUtils.getAnnotation(m, PostConstr.class) != null).forEach(
                im ->{
                    Parameter[] parameters = im.getParameters();
                    Object[] args = new Object[im.getParameterCount()];
                    for (int i = 0; i < parameters.length; i++) {
                        try {
                            args[i] = getInstance(parameters[i].getType());
                        }catch (RuntimeException ex){
                            throw new RuntimeException("准备执行post constr参数时发生异常", ex);
                        }
                    }
                    try {
                        im.invoke(instance, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("执行 post constr 发生异常", e);
                    }
                }
        );
    }

    /**  判断下当前创建的实例是否存在依赖死循环 */
    private void estimateLoop(@NonNull Class<?> elementClazz){
        for (Class<?> clazz : creatingQueue) {

            if (clazz.equals(elementClazz) || elementClazz.isAssignableFrom(clazz)){
                if (log.isErrorEnabled()) {
                    log.error("检测到依赖循环, 要获取的实例正在创建: {}", elementClazz);
                }
                throw new RuntimeException("检测到依赖循环, 要获取的实例正在创建: " + elementClazz);
            }
        }

        if (!creatingQueue.add(elementClazz)) {
            if (log.isErrorEnabled()) {
                log.error("无法将 clazz 加入到创建队列中: {}", elementClazz);
            }
            throw new RuntimeException("无法将 clazz 加入到创建队列中: " + elementClazz);
        }
    }

    /**
     * 实例化, 经过前面的准备来实现具体的实例化
     * @param element 元素对象
     * @param constructorArgMap 构造器 map
     * @param <T> 类型
     * @return 实例化对象
     */
    public <T> T instance(InstanceElement<T> element, Map<Class<?>, Object> constructorArgMap) {
        //找到构造器
        Constructor<T> constructor = obtainConstructor(element);

        if (constructor == null){
            if (log.isErrorEnabled()) {
                log.error("找不到合适的构造器无法创建实例: {}", element);
            }
            throw new InstanceException("无法创建实例, 因为找不到构造器：" + element);
        }

        //构造其的所有参数
        Map<Class<?>, Object> classObjectMap = constructorArgsWrapper(element);
        Object[] args = getArgs(constructor, checkArgsMap(constructorArgMap, classObjectMap));
        return createInstance(constructor, args, element);
    }



    /** 获取合适的构造器 **/
    public <T> Constructor<T> obtainConstructor(InstanceElement<T> instanceElement) {
        return instanceElement.instanceConstructor();
    }

    /***
     * 处理构造器参数
     * @param argsInstanceElementWrapper 构造器 map
     * @return 返回构造器需要的参数 map
     */
    public Map<Class<?>, Object> constructorArgsWrapper(Map<Class<?>, InstanceElement<?>> argsInstanceElementWrapper) {
        Map<Class<?>, Object> constructorArgsMap = new HashMap<>();
        argsInstanceElementWrapper.forEach((k, v) -> constructorArgsMap.put(k, getInstance(v.instanceClass())));
        return constructorArgsMap;
    }


    /**
     * 初始化一个实例, 会通过两个方面进行初始化
     * 1. 通过 spring BeanFactory 进行填充 spring bean
     *    属性, 如果工厂内不存在 spring 依赖, 则会跳过该过程
     * 2. 通过{@link Reflex} 注解, 来通过工厂获取该对象
     * @param instance 要初始化的实例
     * @param elementClass 实例 class
     * @param otherSource 如果需要填充其字段的其他属性
     *                    可以通过 字段名 --> 值 来注入
     * @param <T> 实例类型
     * @return 返回实例对象
     */
    public <T> T initializeInstance(T instance, Class<?> elementClass, Map<String, Object> otherSource) {
        earlyInstanceMutes.remove(elementClass);
        AtomicReference<T> reference = new AtomicReference<>(instance);
        initializeMutes.put(elementClass, new InitializeInstance() {
            @Override
            public <T> T initialize() {
                return (T) doInitializeInstance(reference, elementClass, otherSource);
            }
        });
        return (T) getInstance(elementClass);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    private <T> T doInitializeInstance(AtomicReference<T> instanceReference, Class<?> instanceClass, Map<String, Object> otherSource) {
        if (instanceReference == null)
            return null;
        final T instance = instanceReference.get();
        autoWriedBySpringMutes(instance);
        autoWriedWrapper(instance);

        // TODO: 2021/12/3 handler wriedWrapper
        try {
            fuseInstance(instance, otherSource);
        }catch (RuntimeException ex){
            throw new InitializationException(ex.getMessage(), ex);
        }
        initializeMutes.remove(instanceClass);
        return instance;
    }

    private void fuseInstance(Object instance, Map<String, Object> otherSource){

        if (instance == null || otherSource == null || otherSource.isEmpty())
            return;

        final Class<?> instanceClass = instance.getClass();
        otherSource.forEach((name, value) ->{

            Field f;
            try {
                f = instanceClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if(!f.isAccessible()){
                f.setAccessible(true);
            }
            try {
                f.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });

    }

    private Object[] getArgs(Constructor<?> constructor, Map<Class<?>, Object> argsMap){

        if (constructor == null)
            return null;
        Object[] constructorArgs = new Object[constructor.getParameterCount()];
        if (constructorArgs.length == 0)
            return constructorArgs;

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Set<Class<?>> typeSet = argsMap.keySet();
        for (int i = 0; i < parameterTypes.length; i++) {

            if (typeSet.contains(parameterTypes[i])){
                constructorArgs[i] = argsMap.get(parameterTypes[i]);
            }
        }
        return constructorArgs;
    }

    private Map<Class<?>, Object> checkArgsMap(Map<Class<?>, Object> constructorArgMap, Map<Class<?>, Object> classObjectMap){
        if (constructorArgMap == null && classObjectMap != null){
            return classObjectMap;
        }else if (classObjectMap == null && constructorArgMap != null)
            return constructorArgMap;
        else if (classObjectMap == null && constructorArgMap == null)
            return null;

        Set<Class<?>> classes = constructorArgMap.keySet();
        for (Class<?> aClass : classes) {
            if (classObjectMap.containsKey(aClass)){
                String message = "构造器参数类型不能重复多个, 只能识别单个 class";
                if (log.isErrorEnabled()) {
                    log.error(message);
                }
                throw new InstanceException(message);
            }
        }
        constructorArgMap.putAll(classObjectMap);
        return constructorArgMap;
    }

    private <T> T createInstance(Constructor<T> constructor, Object[] args, InstanceElement<T> element){
        if (constructor == null)
            return null;
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InstanceException("实例化对象: " + element.instanceClass() + ",过程中发生异常:" + e.getMessage(), e);
        }
    }

    /**
     * initializeInstance 内部的一个子方法, 第二部的具体实现
     * @param instance 要填充的实例
     */
    @Override
    public void autoWriedWrapper(Object instance){
        if(AnnotationUtils.getAnnotation(instance.getClass(), AllowCheckReflex.class) != null){
            List<Field> fields = ReflexHandler.getAccessibleFields(instance);
            fields.stream().filter(f ->
            {
                try {
                    return  AnnotationUtils.getAnnotation(f, Reflex.class) != null &&
                            AnnotationUtils.getAnnotation(f, Autowired.class) == null &&
                            AnnotationUtils.getAnnotation(f, Resource.class) == null &&
                            f.get(instance) == null;
                } catch (IllegalAccessException e) {
                    throw new InitializationException(e.getMessage(), e);
                }
            }).forEach(
                    field -> {
                        final Class<?> fieldClass = field.getType();
                        if (instance.getClass().equals(fieldClass)){
                            try {
                                field.set(instance, instance);
                            } catch (IllegalAccessException e) {
                                throw new InitializationException(e.getMessage(), e);
                            }
                        }

                        Object autoWriedVal = getInstance(fieldClass);
                        if (autoWriedVal != null) {
                            try {
                                field.set(instance, autoWriedVal);
                            } catch (IllegalAccessException e) {
                                throw new InitializationException(e.getMessage(), e);
                            }
                        }
                    }
            );
        }
    }

    /** 需要 spring 的 bean 来注入, 一般是被{@link javax.annotation.Resource}
     * 注解的字段
     * */
    @Override
    public void autoWriedBySpringMutes(Object instance){
        if (defaultListableBeanFactory != null)
            defaultListableBeanFactory.autowireBean(instance);
    }


    @Override
    public void registerBean(Class<?> key, Object bean) {

    }

    /**
     * 删除一个实例的方法
     * @param targetClass 目标的 class 对象
     * @return 如果为 false 表示删除失败
     */
    @Override
    public Object remove(Class<?> targetClass) {
        Class<?> hitClassSpectrum = ((ClassMappingFactory)getParent()).hitClassSpectrum(targetClass, instanceMutes.keySet());

        if (hitClassSpectrum != null){
            instanceMutes.remove(hitClassSpectrum);
            return true;
        }

        if ((hitClassSpectrum = ((ClassMappingFactory)getParent()).hitClassSpectrum(targetClass, creatingQueue)) != null){
            creatingQueue.remove(hitClassSpectrum);
            return true;
        }
        return false;
    }

    @Override
    public void merga(CacheFactory<Class<?>, Object> otherFactory) {

    }

    @Override
    protected void doConvert(Map<Class<?>, Object> targetSource) {

    }

    @Override
    protected void doMerge(Map<Class<?>, Object> targetSource) {

    }

    @Override
    public InstanceWrapper<?> createWrapper(Class<?> beanClass) {
        return null;
    }

    @Override
    public InstanceWrapper<?> createWrapper(Class<?> beanClass, Map<String, Object> contructMapArgs) {
        return null;
    }

    @Override
    public InstanceWrapperFactory getWrapperFactory() {
        return instanceElementFactory;
    }

    @Override
    public Object get(InstanceWrapper<?> p) {
        return null;
    }

    @Override
    public Object getBean(Class<?> key) {
        return getSington(key);
    }

    @Override
    public Object getSington(Class<?> key) {
        return obtainSington(key);
    }


}

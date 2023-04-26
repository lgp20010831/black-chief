package com.black.core.factory.beans;

import com.black.condition.ConditionalEngine;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.process.inter.BeanFactoryProcessor;
import com.black.core.factory.beans.process.inter.BeanPostProcessor;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.MethodWrapper;
import lombok.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/***
 *First, the retrieval phase
 *Check whether the type already exists or a subset exists in beanfactory
 *If any, return directly
 *
 *The second stage generates class encapsulation objects
 *And permanently cache the encapsulated object
 *
 *Phase III
 *Traverse beanpostprocessor
 *Define beforeinstance, afterinstance, beforeinitialize, afterinitialize, aboutfactory
 *
 * 3.1
 * beforeInstance
 *
 *Instantiate, inject constructor parameters
 *
 *Instantiation complete
 *
 * afterInstance
 *
 *Check the bean type
 *If the instancecomplete method is called,
 *
 * beforeInitialize
 *Initialize
 *Initialization complete
 * afterInitialize
 *Check the bean type
 *If it is initializebean, call initializecomplete
 *If it is a factorybean, call GetObject
 *
 *Finally, registerbean
 * 作者: 李桂鹏
 */
public interface BeanFactory {


    /***
     * This method is freely defined by subclasses.
     * The return type and parameter type are
     * not based on the principle of taking class as
     * the only parameter
     * @param param Any defined type
     * @return Any type of object
     */
    Object get(Object param);

    /***
     * Get objects, According to class type
     * @param genealogyClass All class objects on the implementation
     *                       and inheritance chain
     * @param <B> class type
     * @return All objects that match this type
     */
    <B> List<B> getBean(Class<B> genealogyClass);

    /***
     * Get an object, According to class type
     * @param genealogyClass All class objects on the implementation
     *                       and inheritance chain
     * @param <B> class type
     * @return Get a singleton object. If there are multiple objects,
     *         select one at random
     */
    <B> B getSingleBean(Class<B> genealogyClass);

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
    <B> List<B> getBean(Class<B> genealogyClass, Map<String, Object> tempSource);

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
    <B> B getSingleBean(Class<B> genealogyClass, Map<String, Object> tempSource);


    /***
     * In order to create the factory and enjoy the freedom of object
     * creation, we can not only create the factory, but also pull the
     * object into the factory
     * @param genealogyClass All class objects on the implementation and inheritance chain
     * @param <B> class type
     * @return Returns an object that has been created
     */
    <B> B prototypeCreateBean(Class<B> genealogyClass);

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
    <B> B prototypeCreateBean(Class<B> genealogyClass, Map<String, Object> tempSource);

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
    <B> B doCreateBean(BeanDefinitional<?> definitional, Map<String, Object> tempSource);

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
    <B> B createBean(Class<B> genealogyClass, Map<String, Object> tempSource);


    Object doInstanceBean(Object[] constructorArgs, ConstructorWrapper cw, BeanDefinitional<?> definitional);

    /***
     * Create an object identity definition
     * @param genealogyClass specific types
     * @param prototype Is it a prototype object
     * @param <B> class type
     * @return object identity definition
     */
    <B> BeanDefinitional<B> createDefinitional(Class<B> genealogyClass, boolean prototype);

    /***
     * returns all created instances
     * @return all created instances
     */
    Map<Class<?>, Object> getMutes();

    /***
     * Register an identity definition, If it already exists, an exception will be thrown
     * @param definitional an identity definition
     */
    void registerDefinitional(@NonNull BeanDefinitional<?> definitional);

    /***
     * Get bean processor
     * @return bean processor list
     */
    Collection<BeanPostProcessor> getBeanProcessors();

    /***
     * Get factory processor
     * @return factory processor list
     */
    Collection<BeanFactoryProcessor> getBeanFactoryProcessors();

    /***
     * Object instantiation preprocessing
     * @param definitional object identity definition
     * @return bean
     */
    Object processorBeforeInstance(BeanDefinitional<?> definitional);

    /***
     * Instantiate an object, excluding initialization
     * @param definitional object identity definition
     * @param tempSource If the object needs to be constructed,
     *                   this attribute serves as an additional
     *                   padding for the constructor
     * @return bean
     */
    Object instanceBean(BeanDefinitional<?> definitional, Map<String, Object> tempSource);

    /***
     * Get constructor parameters
     * @param constructorDefinitionalMap Constructor definition
     * @param tempSource source
     * @param constructorWrapper constructor wrapper
     * @return parameter list
     */
    Object[] getConstructorArgs(Map<ParameterWrapper, BeanDefinitional<?>> constructorDefinitionalMap,
                                Map<String, Object> tempSource,
                                ConstructorWrapper<?> constructorWrapper);

    /***
     * Execute constructor post-processing
     * @param bean bean
     * @param definitional object identity definition
     */
    void invokeBeanPostConstructor(Object bean, BeanDefinitional<?> definitional);

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
    boolean processorAfterInstance(Object dryBean, BeanDefinitional<?> definitional);

    /***
     * Instantiation completion processing object
     * @param bean uninitialized object
     * @param definitional object identity definition
     */
    void completeBean(Object bean, BeanDefinitional<?> definitional);

    /***
     * Initialize preprocessing
     * @param dryBean uninitialized object
     * @param definitional object identity definition
     * @return  the bean instance to use, either the original or a wrapped one; if null, no subsequent BeanPostProcessors will be invoked
     */
    Object processorBeforeInitialize(Object dryBean, BeanDefinitional<?> definitional);

    /***
     * gets the condition engine supported by the current factory
     * @return condition engine
     */
    ConditionalEngine getConditionalEngine();


    /**
     * Performs a task and is constrained by the condition processor
     * Only when the conditions are met will it be implemented
     * @param element conditionally mounted objects
     * @param source environmental data for condition judgment
     * @param callable task
     * @param <C> result type
     * @return task result
     */
    <C> C callConditions(AnnotatedElement element, Object source, Callable<C> callable);

    /***
     * Performs a task and is constrained by the condition processor
     * Only when the conditions are met will it be implemented
     * @param element conditionally mounted objects
     * @param source environmental data for condition judgment
     * @param runnable task
     */
    void runConditions(AnnotatedElement element, Object source, Runnable runnable);

    /***
     * Initialize bean
     * @param bean uninitialized object
     * @param definitional object identity definition
     * @return bean
     */
    Object initializeBean(Object bean, BeanDefinitional<?> definitional);

    /***
     * Initialize post-processing
     * @param completeBean Initialization completed object
     * @param definitional object identity definition
     * @return bean
     */
    Object processorAfterInitialize(Object completeBean, BeanDefinitional<?> definitional);

    /***
     * Initialization completion processing
     * @param bean bean
     * @return bean
     */
    Object shapingBean(Object bean);

    /***
     * Registration object itself and its mapping relationship
     * @param bean bean
     */
    void registerBean(Object bean);


    /***
     * prepare the parameters required for the execution of a method
     * @param args Source parameters, and subsequent processors will
     *             perform different Jia based on metadata
     * @param bean as bean
     * @param mw method
     * @return args
     */
    Object[] prepareMethodParams(Object[] args, Object bean, MethodWrapper mw);

    /***
     * Execution object method
     * @param bean bean
     * @param methodWrapper methodWrapper
     * @return result
     */
    Object invokeBeanMethod(Object bean, MethodWrapper methodWrapper);

    /***
     * result value of processing method
     * @param bean as bean
     * @param value return value
     * @param mw target method
     * @return return value after machining
     */
    Object afterInvokeMethod(Object bean, Object value, MethodWrapper mw);

    /***
     * Does this type of object exist
     * @param type type
     * @return is contain
     */
    boolean containBean(Class<?> type);

    /***
     * fill an object
     * @param bean bean
     */
    void autoWriedBean(Object bean);

    /***
     * get definitional
     * @param bean bean
     * @return object identity definition
     */
    BeanDefinitional<?> getDefinitional(Object bean);

    /***
     * get definitional
     * @param beanClass beanClass
     * @return object identity definition
     */
    BeanDefinitional<?> getDefinitional(Class<?> beanClass);

    /***
     * clear all caches
     */
    void clearAll();

    /***
     * Destroy an object
     * @param bean bean
     */
    void destroyBean(Object bean);

    /***
     * Delete the existence of the object in the
     * factory and clear its mapping relationship
     * @param type type
     */
    void removes(Class<?> type);

    /***
     * Register bean processor
     * @param processor processor
     */
    void registerBeanLifeCycleProcessor(BeanPostProcessor processor);

    //has many bean
    int size();

    /***
     * Register factory processor
     * @param processor processor
     */
    void registerBeanFactoryProcessor(BeanFactoryProcessor processor);
}

package com.black.aviator;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.BeanDefinitional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.spring.factory.ReusingProxyFactory;

import java.util.Map;

public class AviatorContext {

    public static final String[] initialEnvNames = new String[]{
            "init", "create", "set", "c", "C"
    };

    public static final String[] fieldEnvNames = new String[]{
            "field", "f", "F"
    };

    public static final String[] methodResultEnvNames = new String[]{
            "result", "r", "R"
    };

    public static final String[] paramEnvNames = new String[]{
            "param", "p", "P"
    };

    private final BeanFactory beanFactory;

    private final ReusingProxyFactory proxyFactory;

    /**
     * 功能: 作为条件解析表达式
     *      同时具备两种方式
     *      1. boolean 表达式, 即返回值为布尔值
     *          对于结果会生成两条分支
     *          最好的解决方法是  xx ? xx : xx
     *     2. object 表达式, 返回值为任意类型
     *
     *     解析表达式需要环境变量
     *     当一个类被代理生成时, 便会生成一份环境变量
     *     环境变量 = 构造器参数  + 所有属性变量
     *
     *
     *     注入点
     *     作用类上 beanFactory 可能会适用
     *
     *     作用字段上, beanFactory 在注入的时候可能会走此逻辑
     *               如果被表达式代理,  则会以环境变量作为 map 属性, 解析表达式
     *               字段上只允许标注 Object 表达式
     *
     *      作用在方法上
     *                  布尔值表达式 表示拦截方法, 返回 true 则表示执行该方法, false 表示拦截方法 之后会走 otherwise 返回结果
     *                   Object 表达式会以 {result = 方法结果作为变量},  目地替换掉返回值
     *
     *      作用在参数:
     *                  Object 替换掉参数
     *                  boolean 判断 然后走 then 或者 esle 目地都是替换掉参数
     *
     *
     *
     *      实例化：
     *          由自己寻找构造器或者交给 beanFactory 寻找构造器
     *
     *
     */

    public AviatorContext() {
        FactoryManager.init();
        beanFactory = FactoryManager.getBeanFactory();
        proxyFactory = FactoryManager.getProxyFactory();
    }

    public <T> T create(Class<T> type){
        return create(type, null);
    }

    public <T> T create(Class<T> type, Map<String, Object> tempSource){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        ObjectEnv objectEnv = new ObjectEnv();

        //创建一个身份定义
        BeanDefinitional<T> definitional = beanFactory.createDefinitional(type, false);

        //设置该对象为需要代理, 并设置代理处理器
        authority(definitional);
        ConstructorWrapper<?> constructorWrapper;
        try {
             //自动寻找合适的构造器
             constructorWrapper = definitional.mainConstructor();
        } catch (NoSuchMethodException e) {
            throw new AviatorsException("无法找到该对象的构造器", e);
        }

        //根据指定构造器创建出要创建需要的模板信息
        Map<ParameterWrapper, BeanDefinitional<?>> beanDefinitionalMap = definitional.instanceConstructorWrapper(constructorWrapper, beanFactory);

        //根据模板信息和环境变量创建出实例化需要的参数
        Object[] constructorArgs = beanFactory.getConstructorArgs(beanDefinitionalMap, tempSource, constructorWrapper);

        //环境对象解析实例化参数
        objectEnv.parseCreateParams(constructorWrapper, constructorArgs);

        //通过对象工厂实例化对象, 完成代理
        T proxy = (T) beanFactory.doInstanceBean(constructorArgs, constructorWrapper, definitional);

        //通过对象工厂初始化对象
        beanFactory.initializeBean(proxy, definitional);

        //环境对象解析代理对象属性
        objectEnv.parseFieldParams(proxy, cw);
        //将环境对象赋值给代理处理器
        AviatorAgentLayer agentLayer = (AviatorAgentLayer) definitional.getAgentLayer();
        agentLayer.setObjectEnv(objectEnv);
        return proxy;
    }

    public <T> T create(Class<T> type, Class<?>[] paramTypes, Object[] args){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        ObjectEnv objectEnv = new ObjectEnv();
        //根据指定参数列表寻找构造器
        ConstructorWrapper<?> constructorWrapper = cw.getConstructor(paramTypes);
        if (constructorWrapper == null){
            throw new AviatorsException("无法找到该对象的构造器");
        }
        objectEnv.parseCreateParams(constructorWrapper, args);
        AviatorAgentLayer agentLayer;
        //通过代理工厂去代理对象
        T proxy = proxyFactory.proxy(type, paramTypes, args, agentLayer = new AviatorAgentLayer());
        objectEnv.parseFieldParams(proxy, cw);
        agentLayer.setObjectEnv(objectEnv);
        return proxy;
    }


    private void authority(BeanDefinitional<?> definitional){
        Class<? extends BeanDefinitional> definitionalClass = definitional.getClass();
        ClassWrapper<? extends BeanDefinitional> cw = ClassWrapper.get(definitionalClass);
        FieldWrapper agent = cw.getField("agent");
        if (agent == null){
            throw new IllegalStateException("无法替换对象代理信息");
        }
        agent.setValue(definitional, true);
        FieldWrapper layerType = cw.getField("layerType");
        if (layerType != null){
            layerType.setValue(definitional, AviatorAgentLayer.class);
        }
    }
}

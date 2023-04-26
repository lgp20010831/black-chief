package com.black.arg;

import com.black.arg.custom.CustomParameterProcessor;
import com.black.arg.filter.DepthAnalysisFieldFilter;
import com.black.arg.original.OriginalArgStrategy;
import com.black.arg.original.OriginalArgStrategyHandler;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.config.CentralizedProcessorConfiguringAttributeInjector;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Av0;
import com.black.holder.SpringHodler;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.utils.CollectionUtils;
import com.black.utils.NameUtil;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

//方法反射入参处理器
@Getter @Setter
public class MethodReflectionIntoTheParameterProcessor {

    /*
        思路:
        参数分为 原始参数 arg[]
                匹配时的策略:
                1.类型相同即可
                2.下标相同即可, 类型进行转换(为空时从环境获取)
                3.下标相同即可, 类型进行转换(为空时直接返回空)

               Map<String, Object> 环境参数

               关于这个key代表什么意思:
               1. 参数名称
               2. 注解含义
               3. 无特别含义, 直接去 value[0] 即可

               针对这个 value
               是否深度解析 -- 解析成什么呢?
               当 meaning =  参数名称 时 --》 json
                         =  注解含义 --> json

               解析的时候, 什么是需要的, 什么是不需要的, 需要加过滤器

               //无法识别的参数将从其他地方获取
               是否需要从其他环境获取, 需要加拦截器
               1. 配置 spring 环境, 将尝试从 spring 容器获取
               2. 配置 chief bean factory 环境, 将从 bean factory 里创建


               对于创建的对象是否进行 config 处理
     */

    //原始参数 类型优先
    private boolean originArgTypeFirst = true;

    private IoLog log = LogFactory.getArrayLog();

    private OriginalArgStrategy originalArgStrategy = OriginalArgStrategy.compatible_type;

    private static final LinkedBlockingQueue<OriginalArgStrategyHandler> handlers = new LinkedBlockingQueue<>();

    private static final LinkedBlockingQueue<DepthAnalysisFieldFilter> fieldFilters= new LinkedBlockingQueue<>();


    static {
        ChiefScanner scanner = ScannerManager.getScanner();
        for (Class<?> clazz : scanner.load("com.black.arg.filter")) {
            if (BeanUtil.isSolidClass(clazz) && DepthAnalysisFieldFilter.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                fieldFilters.add((DepthAnalysisFieldFilter) instance);
            }
        }

        for (Class<?> clazz : scanner.load("com.black.arg.original")) {
            if (BeanUtil.isSolidClass(clazz) && OriginalArgStrategyHandler.class.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                handlers.add((OriginalArgStrategyHandler) instance);
            }
        }
    }

    public void addCustomParameterProcessor(CustomParameterProcessor parameterProcessor){
        customParameterProcessors.add(parameterProcessor);
    }

    private final LinkedBlockingQueue<CustomParameterProcessor> customParameterProcessors = new LinkedBlockingQueue<>();

    private MapKeyMeaning mapKeyMeaning = MapKeyMeaning.no_meaning;

    private boolean typeAsKey = true;

    private boolean depthAnalysis = true;

    private boolean nullValueGetFromSpring = true;

    private boolean nullValueCreateByFactory = true;

    private boolean useConfigTool = false;

    private volatile ConfiguringAttributeAutoinjector autoinjector;

    private Function<ParameterWrapper, String> getParamAliasFunction;


    public Object[] parse(MethodWrapper mw, Object[] args, Object noMeaningBean){
        MapKeyMeaning temp = this.mapKeyMeaning;
        mapKeyMeaning = MapKeyMeaning.no_meaning;
        try {
            return parse(mw, args, Av0.of("null", noMeaningBean));
        }finally {
            mapKeyMeaning = temp;
        }
    }

    public Object[] parse(MethodWrapper mw, MethodWrapper originMethod, Object[] args, Object noMeaningBean){
        MapKeyMeaning temp = this.mapKeyMeaning;
        mapKeyMeaning = MapKeyMeaning.no_meaning;
        try {
            return parse(mw, castToArgMap(originMethod, args), Av0.of("null", noMeaningBean));
        }finally {
            mapKeyMeaning = temp;
        }
    }

    private Map<String, Object> castToArgMap(MethodWrapper originMethod, Object[] args){
        if (args.length != originMethod.getParameterCount()){
            throw new IllegalStateException("Native parameter does not match method: " + originMethod.get());
        }
        Map<String, Object> argMap = new LinkedHashMap<>();
        for (ParameterWrapper parameterWrapper : originMethod.getParameterArray()) {
            argMap.put(parameterWrapper.getName(), args[parameterWrapper.getIndex()]);
        }
        return argMap;
    }

    private Map<String, Object> castToArgMap(Object[] args){
        String prefix = "k";
        int index = 0;
        Map<String, Object> argMap = new LinkedHashMap<>();
        for (Object arg : args) {
            argMap.put(prefix + index++, arg);
        }
        return argMap;
    }

    public Object[] parse(MethodWrapper mw, MethodWrapper originMethod, Object[] args, Map<String, Object> env){
        return parse(mw, castToArgMap(originMethod, args), env);
    }

    public Object[] parse(MethodWrapper mw, Object[] args, Map<String, Object> env){
        return parse(mw, castToArgMap(args), env);
    }

    public Object[] parse(MethodWrapper mw, Map<String, Object> argMap, Map<String, Object> env){
        Object[] newArgs = new Object[mw.getParameterCount()];
        ParameterWrapper[] parameterArray = mw.getParameterArray();
        for (ParameterWrapper pw : parameterArray) {

            Object arg = resolveArg(mw, argMap, env, pw);
            newArgs[pw.getIndex()] = arg;
        }
        return newArgs;
    }

    protected Object resolveArg(MethodWrapper mw, Map<String, Object> argMap, Map<String, Object> env, ParameterWrapper pw){
        Object arg = null;
        for (CustomParameterProcessor processor : customParameterProcessors) {
            if (processor.support(pw)) {
                arg = processor.getArg(pw, mw, argMap, env);
            }
            if (arg != null){
                return arg;
            }
        }

        OriginalArgStrategyHandler strategyHandler = null;
        for (OriginalArgStrategyHandler handler : handlers) {
            if (handler.support(originalArgStrategy)) {
                strategyHandler = handler;
                break;
            }
        }

        if (strategyHandler != null){
            arg = strategyHandler.handler(mw, pw, argMap, this);
        }

        if (strategyHandler != null && !strategyHandler.canNext(arg)){
            return arg;
        }

        arg = resolveEnv(env, pw);

        if (arg != null){
            return arg;
        }

        arg = getFromSpring(pw);
        if (arg != null){
            return arg;
        }

        arg = createArgByFactory(pw);
        if (arg == null){
            return arg;
        }

        configHandle(arg);
        return arg;
    }

    protected void configHandle(Object bean){
        if (!isUseConfigTool()){
            return;
        }
        if (autoinjector == null){
            autoinjector = new CentralizedProcessorConfiguringAttributeInjector();
        }

        autoinjector.pourintoBean(bean);
    }

    protected Object createArgByFactory(ParameterWrapper pw){
        if (!isNullValueCreateByFactory()){
            return null;
        }
        try {
            Class<?> type = pw.getType();
            BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
            if (Map.class.isAssignableFrom(type)){
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
                if (genericVals.length != 2){
                    return null;
                }
                List<?> beans = beanFactory.getBean(genericVals[1]);
                Map<Object, Object> map = ServiceUtils.createMap(type);
                for (Object bean : beans) {
                    map.put(NameUtil.getName(bean), bean);
                }
                return map;
            }else if (Collection.class.isAssignableFrom(type)){
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
                if (genericVals.length != 1){
                    return null;
                }
                Collection<Object> collection = ServiceUtils.createCollection(type);
                collection.addAll(beanFactory.getBean(genericVals[0]));
                return collection;
            }else {
                return beanFactory.getSingleBean(type);
            }
        }catch (Throwable ex){
            log.error("can not create bean of chief factory: {}", ex.getMessage());
            return null;
        }
    }

    protected Object getFromSpring(ParameterWrapper pw){
        if (!isNullValueGetFromSpring()){
            return null;
        }

        DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
        if (beanFactory == null){
            return null;
        }

        Class<?> type = pw.getType();
        try {
            if (Map.class.isAssignableFrom(type)){
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
                if (genericVals.length != 2){
                    return null;
                }
                return BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, genericVals[1]);
            }else if (Collection.class.isAssignableFrom(type)){
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
                if (genericVals.length != 1){
                    return null;
                }
                Collection<Object> collection = ServiceUtils.createCollection(type);
                collection.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, genericVals[0]).values());
                return collection;
            }else {
                return beanFactory.getBean(type);
            }
        }catch (Throwable e){
            return null;
        }
    }

    protected Object resolveEnv(Map<String, Object> env, ParameterWrapper pw){
        Object source;
        switch (mapKeyMeaning){
            case param_name:
                String name = pw.getName();
                source = env.get(name);
               break;
            case annotation:
                if (getParamAliasFunction == null){
                    return null;
                }

                String apply = getParamAliasFunction.apply(pw);
                source = env.get(apply);
                break;
            case no_meaning:
                source = CollectionUtils.firstValue(env);
                break;
            default:
                throw new IllegalStateException("ill: " + mapKeyMeaning);
        }
        if (source == null){
            return null;
        }
        return processorArg(source, pw);
    }

    protected Object processorArg(Object source, ParameterWrapper pw){
        if (!isDepthAnalysis()){
            return source;
        }

        if (isTypeAsKey()){
            Map<Class<?>, Object> map = new LinkedHashMap<>();
            parseObjectTypeAsKey(source, map);
            Class<?> type = pw.getType();
            for (Class<?> keyType : map.keySet()) {
                Object val = map.get(keyType);
                if (type.isAssignableFrom(keyType)){
                    return val;
                }
            }
            return null;
        }else {
            Map<String, Object> map = new LinkedHashMap<>();
            parseObjectNameAsKey(source, map);
            Object val = map.get(pw.getName());
            return TypeConvertCache.initAndGet().convert(pw.getType(), val);
        }
    }


    private void parseObjectNameAsKey(Object source, Map<String, Object> result){
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(source);
        for (FieldWrapper field : classWrapper.getFields()) {
            Object value = field.getValue(source);
            String name = field.getName();
            if (result.containsKey(name)){
                continue;
            }
            if (value != null && canAnalysis(field, source)) {
                parseObjectNameAsKey(value, result);
            }
            result.put(field.getName(), value);
        }
    }

    /*
        class User{

            User user;
        }

     */

    private void parseObjectTypeAsKey(Object source, Map<Class<?>, Object> result){
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(source);
        Class<?> primordialClass = classWrapper.getPrimordialClass();
        if (result.containsKey(primordialClass)){
            return;
        }
        result.put(primordialClass, source);
        for (FieldWrapper field : classWrapper.getFields()) {
            Object value = field.getValue(source);
            Class<?> type = field.getType();
            if (value != null && !field.getType().equals(classWrapper.get())
                    && canAnalysis(field, source)) {
                parseObjectTypeAsKey(value, result);
            }
            result.put(field.getType(), value);
        }
    }




    protected boolean canAnalysis(FieldWrapper fw, Object bean){
        for (DepthAnalysisFieldFilter filter : fieldFilters) {
            if (!filter.canAnalysis(fw, bean)) {
                return false;
            }
        }
        return true;
    }
}

package com.black.core.aop.ibatis.servlet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.core.aop.servlet.GlobalAround;
import com.black.core.aop.servlet.GlobalAroundResolver;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.json.ReflexUtils;
import com.black.core.mybatis.plus.*;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Av0;
import com.black.core.util.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@GlobalAround
public class ArrayQueryGlobalResolver implements GlobalAroundResolver {

    private Boolean cancel;
    private final DefaultListableBeanFactory beanFactory;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<Method, QueryFactory> cache = new ConcurrentHashMap<>();


    public ArrayQueryGlobalResolver(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object[] beforeInvoke(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        if (isCancel()){
            return GlobalAroundResolver.super.beforeInvoke(args, httpMethodWrapper);
        }

        Method method = httpMethodWrapper.getHttpMethod();
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        lock.lock();
        try {
            parseMethod(method);
        }finally {
            lock.unlock();
        }

        if (cache.containsKey(method)){
            ParameterWrapper parameterWrapper = wrapper.getSingleParameterByAnnotation(OpenFactory.class);
            if (parameterWrapper != null && QueryFactory.class.isAssignableFrom(parameterWrapper.getType())){
                args[parameterWrapper.getIndex()] = cache.get(method);
            }
        }
        return GlobalAroundResolver.super.beforeInvoke(args, httpMethodWrapper);
    }


    protected boolean isCancel(){
        if (cancel == null){
            try {
                Class.forName("com.baomidou.mybatisplus.core.conditions.query.QueryWrapper");
                cancel = false;
            } catch (ClassNotFoundException e) {
                cancel = true;
            }
        }
        return cancel;
    }

    protected void parseMethod(Method method){
        ConfigurationsFactory configurations = AnnotationUtils.getAnnotation(method, ConfigurationsFactory.class);
        if(configurations == null){
            return;
        }

        if (cache.containsKey(method)){
            return;
        }
        ArrayQueryFactory queryFactory = new ArrayQueryFactory();

        //全局映射
        Set<String> globalMapping = new HashSet<>(Av0.as(configurations.masterSlaveMapping()));

        Query[] querys = configurations.querys();
        if (querys.length != 0){
            Collection<Configuration> configurationList = new ArrayList<>();
            for (Query query : querys) {
                ConfigurationFactory factory = new ConfigurationFactory();
                Class<? extends BaseMapper<?>> mapperClass = query.mapper();
                BaseMapper<?> baseMapper;
                try {

                    baseMapper = beanFactory.getBean(mapperClass);
                }catch (BeansException ex){
                    throw new ArrayQueryException("无法获取 plus mapper", ex);
                }
                factory.pointMapper(baseMapper);
                boolean defaultSet = query.defaultSet();
                //添加映射关系
                String[] mapping = query.masterSlaveMapping();
                if (mapping.length == 0){
                    mapping = megerMapping(globalMapping, configurations.masterSlaveMapping());
                }
                Map<String, String> configMapping = factory.addMapping(mapping);

                //添加 and 条件
                String[] and = query.andOperatorQuery();
                if (and.length == 0){
                    and = configurations.andOperatorQuery();
                }
                if (and.length == 0 && defaultSet){
                    and = configMapping.keySet().toArray(new String[0]);
                }

                factory.addAnd(and);

                //添加 or 条件
                String[] or = query.orOperatorQuery();
                if (or.length == 0){
                    or = configurations.orOperatorQuery();
                }
                factory.addOr(or);

                //添加正序排序
                String[] orderByAsc = query.orderByAsc();
                if (orderByAsc.length == 0){
                    orderByAsc = configurations.orderByAsc();
                }
                factory.addOrderAsc(orderByAsc);

                //添加到序排序
                String[] orderByDesc = query.orderByDesc();
                if (orderByDesc.length == 0){
                    orderByDesc = configurations.orderByDesc();
                }
                factory.addOrderDesc(orderByDesc);
                Configuration configuration = factory.open();

                String groupBy = query.groupBy();
                if (!StringUtils.hasText(groupBy)){
                    groupBy = configurations.groupBy();
                }
                if (!StringUtils.hasText(groupBy) && configMapping.size() == 1 && defaultSet){
                    groupBy = configMapping.keySet().toArray(new String[0])[0];
                }
                configuration.setGroupBy(groupBy);

                //注册条件 map
                String[] map = query.conditionMap();
                if (map.length == 0){
                    map = configurations.conditionMap();
                }
                Map<String, ConditionValue> conditionValueMap = parseConditionMap(map, configuration);
                configuration.getConditionMap().putAll(conditionValueMap);

                configuration.setIn(query.in());
                String mapKey = query.mapKey();
                if (!StringUtils.hasText(mapKey) && defaultSet){
                    mapKey = StringUtils.linkStr(StringUtils.titleLower(mapperClass.getSimpleName()), "Values");
                }
                configuration.setMapKey(mapKey);
                configurationList.add(configuration);
            }
            queryFactory.addAll(configurationList);
        }
        cache.put(method, new QueryFactory(queryFactory));
    }

    protected Map<String, ConditionValue> parseConditionMap(String[] entrys, Configuration configuration){
        Map<String, ConditionValue> result = new HashMap<>();
        Class<?> entity = configuration.getEntity();
        Set<String> fieldNames = configuration.getFieldNames();
        for (String entry : entrys) {
            boolean and, like;
            String name, value;
            if (entry.contains("=")){
                like = false;
                String[] nv = entry.split("=");
                name = nv[0].trim();
                value = nv[1].trim();
            }else if (entry.contains("like")){
                like = true;
                String[] nv = entry.split("like");
                name = nv[0].trim();
                value = nv[1].trim();
            }else {
                throw new ParseConfigurationException("条件 map 运算符应该从 = 或者 like 中选择");
            }
            and = !name.startsWith("!");
            if (!and){
                name = name.substring(1);
            }
            if (fieldNames.contains(name)){
                Field field = ReflexUtils.getField(name, entity);
                Class<?> type = field.getType();
                Object val = value;
                if (String.class.isAssignableFrom(type)){
                    TypeHandler typeHandler = TypeConvertCache.initAndGet();
                    val = typeHandler.convert(type, value);
                }
                ConditionValue conditionValue = new ConditionValue(val, and, like);
                result.put(name, conditionValue);
            }
        }
        return result;
    }

    protected String[] megerMapping(Set<String> globalMapping, String[] mapping){
        Set<String> mappingSet = new HashSet<>(Av0.as(mapping));
        mappingSet.addAll(globalMapping);
        return mappingSet.toArray(new String[0]);
    }
}

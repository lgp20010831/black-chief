package com.black.sql_v2.javassist.aop;

import com.black.config.AttributeUtils;
import com.black.pattern.NameAndValue;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.chain.GroupKeys;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Utils;
import com.black.sql_v2.*;
import com.black.sql_v2.javassist.Agentable;
import com.black.utils.CollectionUtils;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;

import java.lang.reflect.Method;
import java.util.*;

public class SqlV2AopUtils {


    public static boolean isAgentable(Method method){
        return AnnotationUtils.isPertain(method, Agentable.class);
    }


    public static GroupKeys parseToTableNameAndAlias(Method method, Class<?> clazz, Object bean){
        String tableName = null;
        String alias = null;
        if (Opt.class.isAssignableFrom(clazz)){
            Opt opt = (Opt) bean;
            alias = opt.getAlias();
        }

        if (TableNameOpt.class.isAssignableFrom(clazz)){
            TableNameOpt opt = (TableNameOpt) bean;
            tableName = opt.getTableName();
        }

        if (tableName != null && alias != null){
            String methodName = method.getName();
            return new GroupKeys(alias, tableName, methodName);
        }
        return null;
    }

    public static Map<Class<?>, List<NameAndValue>> prepareAttribute(String alias){
        SqlExecutor executor = Sql.opt(alias);
        Environment environment = executor.getEnvironment();
        Map<Class<?>, List<NameAndValue>> executorAttr = AttributeUtils.collectAttributes(executor);
        Map<Class<?>, List<NameAndValue>> envAttr = AttributeUtils.collectAttributes(environment);
        AttributeUtils.merge(executorAttr, envAttr);
        return executorAttr;
    }

    public static Map<Class<?>, List<NameAndValue>> getAttrByAlias(String alias){
        return prepareAttribute(alias);
    }

    public static Object getArgByAttr(Map<String, Map<Class<?>, List<NameAndValue>>> attrs, ParameterWrapper pw){
        Class<?> type = pw.getType();
        String name = pw.getName();

        if (Map.class.isAssignableFrom(type)){
            Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
            if (genericVals.length != 2){
                return new HashMap<>();
            }

            if (!genericVals[0].equals(String.class)){
                throw new IllegalStateException("param generice type 0 is not alias string");
            }

            Class<?> genericVal = genericVals[1];
            Map<String, Object> map = ServiceUtils.createMap(type);
            for (String alias : attrs.keySet()) {
                Map<Class<?>, List<NameAndValue>> listMap = attrs.get(alias);
                Object arg = getArgByAttr(listMap, genericVal);
                map.put(alias, arg);
            }
            return map;
        }

        if (Collection.class.isAssignableFrom(type)){
            Map<Class<?>, List<NameAndValue>> copy = new LinkedHashMap<>();
            for (Map<Class<?>, List<NameAndValue>> value : attrs.values()) {
                AttributeUtils.merge(copy, value);
            }
            return getArgByAttr(copy, type);
        }

        String alias = findAlias(pw);
        Map<Class<?>, List<NameAndValue>> map = attrs.get(alias);
        if (map == null){
            map = getAttrByAlias(alias);
            attrs.put(alias, map);
        }
        return getArgByAttr(map, type);
    }

    public static String findAlias(ParameterWrapper pw){
        com.black.sql_v2.javassist.Opt annotation = pw.getAnnotation(com.black.sql_v2.javassist.Opt.class);
        return annotation == null ? Sql.DEFAULT_ALIAS : annotation.value();
    }


    public static Object getArgByAttr(Map<Class<?>, List<NameAndValue>> attrs, Class<?> type){
        if (Map.class.isAssignableFrom(type)){
            throw new IllegalStateException("can not resolve map type");
        }

        if (Collection.class.isAssignableFrom(type)){
            Collection<Object> collection = ServiceUtils.createCollection(type);
            Class<?>[] genericVals = ReflectionUtils.genericVal(type, Collection.class);
            if (genericVals.length != 1){
                return collection;
            }
            List<NameAndValue> nameAndValues = attrs.get(genericVals[0]);
            if (Utils.isEmpty(nameAndValues)){
                return collection;
            }

            for (NameAndValue value : nameAndValues) {
                collection.add(value.getValue());
            }
            return collection;
        }

        List<NameAndValue> nameAndValues = attrs.get(type);
        if (Utils.isEmpty(nameAndValues)){
            return null;
        }
        return CollectionUtils.firstElement(nameAndValues).getValue();
    }
}

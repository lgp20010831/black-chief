package com.black.xml.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.javassist.CtAnnotation;
import com.black.utils.ServiceUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 李桂鹏
 * @create 2023-05-31 14:06
 */
@SuppressWarnings("all")
public class ServletGenerateUtils {

    public static final Map<String, Class<?>> typeMappingMap = new ConcurrentHashMap<>();

    static {
        typeMappingMap.put("string", String.class);
        typeMappingMap.put("int", Integer.class);
        typeMappingMap.put("double", Double.class);
        typeMappingMap.put("float", Float.class);
        typeMappingMap.put("short", Short.class);
        typeMappingMap.put("long", Long.class);
        typeMappingMap.put("byte", Byte.class);
        typeMappingMap.put("boolean", Boolean.class);
        typeMappingMap.put("char", Character.class);
        typeMappingMap.put("json", JSONObject.class);
        typeMappingMap.put("jsonObject", JSONObject.class);
        typeMappingMap.put("map", Map.class);
        typeMappingMap.put("list", List.class);
        typeMappingMap.put("collection", Collection.class);
        typeMappingMap.put("array", JSONArray.class);
        typeMappingMap.put("jsonArray", JSONArray.class);
    }

    public static Map<String, Class<?>> getTypeMappingMap(){
        return typeMappingMap;
    }

    public static Class<?> getType(String name){
        return Assert.nonNull(typeMappingMap.get(name), "Unable to find mapped type");
    }

    public static void loadCtAnnotationAttrs(CtAnnotation annotation, Map<String, Object> attrMap){
        AnnotationTypeWrapper wrapper = AnnotationTypeWrapper.get(annotation.getJavaAnnType());
        for (MethodWrapper wrapperMethod : wrapper.getMethods()) {
            String name = wrapperMethod.getName();
            Class<?> returnType = wrapperMethod.getReturnType();
            if (attrMap.containsKey(name)){
                Object value = attrMap.get(name);
                annotation.addField(name, value, returnType);
            }
        }
    }

    public static void putAllAttrs(Map<String, Object> origin, Map<String, Object> target){
        target.forEach((k, v) -> {
            if (!origin.containsKey(k)){
                origin.put(k, v);
            }
        });
    }

    public static void parseParam2(String syntax, MappingMethodInfo methodInfo){
        syntax = syntax.trim();
        boolean required = syntax.startsWith("!");
        syntax = StringUtils.removeIfStartWith(syntax, "!");
        boolean body = syntax.startsWith("?");
        syntax = StringUtils.removeIfStartWith(syntax, "?");
        String[] nameAndType = syntax.split("::");

        Class<?> type = String.class;
        if (nameAndType.length == 2){
            type = getType(nameAndType[1]);
        }
        String name = nameAndType[0];
        if (body){
            if (!type.equals(JSONObject.class) && !type.equals(JSONArray.class)){
                type = JSONObject.class;
            }
        }

        methodInfo.addParam(name, type, required, !body);
    }

    //!id::query(int), !body::body  !id, !body::body
    public static void parseParam(String param, MappingMethodInfo methodInfo){
        boolean required = param.startsWith("!");
        param = StringUtils.removeIfStartWith(param, "!");
        AtomicReference<Class<?>> paramType = new AtomicReference<>(String.class);
        param = ServiceUtils.parseTxt(param, "(", ")", type -> {
            paramType.set(getType(type));
            return "";
        });
        boolean query = true;
        String name;
        String[] splits = param.split("::");
        if (splits.length == 2){
            String split = splits[1];
            if ("query".equalsIgnoreCase(split)){
                query = true;
            }else if ("body".equalsIgnoreCase(split)){
                paramType.set(JSONObject.class);
                query = false;
            }else if ("array".equalsIgnoreCase(split)){
                paramType.set(JSONArray.class);
                query = false;
            }else {
                throw new IllegalArgumentException("Unrecognized parameter request type: " + split);
            }
            name = splits[0];
        }else {
            name = param;
        }
        methodInfo.addParam(name, paramType.get(), required, query);
    }

    public static void main(String[] args) {
        XmlWrapper xmlWrapper = (XmlWrapper) FactoryManager.initAndGetBeanFactory().get("xml-sql/UserController.xml");
        ElementWrapper element = xmlWrapper.getRootElement();
        XmlServletRegister register = XmlServletRegister.getInstance();
        register.parseWrapper("UserController", element);
    }

}

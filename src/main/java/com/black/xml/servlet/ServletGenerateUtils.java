package com.black.xml.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.cache.TypeConvertCache;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.Trust;
import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.javassist.CtAnnotation;
import com.black.swagger.v2.V2Swagger;
import com.black.utils.ServiceUtils;
import com.black.xml.crud.TypeRegister;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
        MappingMethodInfo.RequestParamInfo paramInfo = parseParam2(syntax);
        methodInfo.addParam(paramInfo);
    }

    public static MappingMethodInfo.RequestParamInfo parseParam2(String syntax){
        syntax = syntax.trim();
        boolean required = syntax.startsWith("!");
        syntax = StringUtils.removeIfStartWith(syntax, "!");
        boolean body = syntax.startsWith("?");
        syntax = StringUtils.removeIfStartWith(syntax, "?");
        TypeRegister typeRegister = TypeRegister.getInstance();
        String[] paramAndAnnotation = syntax.split("@");
        List<CtAnnotation> ctAnnotations = new ArrayList<>();
        if (paramAndAnnotation.length > 1){
            String annotationInfo = paramAndAnnotation[1];
            String[] annotations = annotationInfo.split("&");
            for (String annotation : annotations) {
                annotation = annotation.trim();
                JSONObject attrs = new JSONObject();
                if (annotation.endsWith(")") && annotation.contains("(")) {
                    String fieldInfo = annotation.substring(annotation.indexOf("(") + 1, annotation.lastIndexOf(")"));
                    annotation = annotation.substring(0, annotation.indexOf("("));
                    String[] kvs = fieldInfo.split(",");
                    for (String fs : kvs) {
                        String[] kv = fs.split(":");
                        if (kv.length != 2){
                            throw new IllegalStateException("Parsing attribute format error:" + fs);
                        }
                        attrs.put(kv[0].trim(), kv[1].trim());
                    }
                }

                Class<? extends Annotation> annotationType = typeRegister.getAnnotationType(annotation);
                CtAnnotation ctAnnotation = new CtAnnotation(annotationType);
                AnnotationTypeWrapper typeWrapper = AnnotationTypeWrapper.get(annotationType);
                attrs.forEach((k, v) -> {
                    MethodWrapper methodWrapper = typeWrapper.select(k);
                    if (methodWrapper == null){
                        throw new IllegalStateException("Unable to find annotated property method: " + k);
                    }
                    Class<?> returnType = methodWrapper.getReturnType();
                    v = TypeConvertCache.initAndGet().convert(returnType, v);
                    ctAnnotation.addField(k, v, returnType);
                });
                ctAnnotations.add(ctAnnotation);
            }
        }
        String param = paramAndAnnotation[0];
        String[] nameAndType = param.split("::");

        Class<?> type = String.class;
        if (nameAndType.length == 2){
            type = typeRegister.getType(nameAndType[1]);
        }
        String name = nameAndType[0];
        if (body){
            if (!type.equals(JSONObject.class) && !type.equals(JSONArray.class)){
                type = JSONObject.class;
            }
        }
        MappingMethodInfo.RequestParamInfo paramInfo = new MappingMethodInfo.RequestParamInfo(name, type, required, !body);
        if (!ctAnnotations.isEmpty()){
            paramInfo.addAnnotations(ctAnnotations.toArray(new CtAnnotation[0]));
        }
        return paramInfo;
    }

    //!id::query(int), !body::body  !id, !body::body
    public static void parseParam(String param, MappingMethodInfo methodInfo){
        TypeRegister typeRegister = TypeRegister.getInstance();
        boolean required = param.startsWith("!");
        param = StringUtils.removeIfStartWith(param, "!");
        AtomicReference<Class<?>> paramType = new AtomicReference<>(String.class);
        param = ServiceUtils.parseTxt(param, "(", ")", type -> {
            paramType.set(typeRegister.getType(type));
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
        test();
    }

    static void test(){
        System.out.println(parseParam2("!?body@V2Swagger(value:user{})&notnull"));
    }

    static void xml(){
        XmlWrapper xmlWrapper = (XmlWrapper) FactoryManager.initAndGetBeanFactory().get("xml-sql/UserController.xml");
        ElementWrapper element = xmlWrapper.getRootElement();
        XmlServletRegister register = XmlServletRegister.getInstance();
        register.parseWrapper("UserController", element);
    }

}

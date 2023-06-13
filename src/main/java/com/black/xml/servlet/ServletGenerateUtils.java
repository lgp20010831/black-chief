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
import com.black.core.sql.code.parse.BlendObjects;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.generic.Generic;
import com.black.generic.GenericInfo;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.swagger.v2.V2Swagger;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;
import com.black.xml.crud.TypeRegister;
import lombok.NonNull;

import javax.sql.rowset.CachedRowSet;
import java.lang.annotation.Annotation;
import java.util.*;
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

    public static CtAnnotations parseAnnotations(String desc){
        TypeRegister typeRegister = TypeRegister.getInstance();
        CtAnnotations ctAnnotations = new CtAnnotations();
        String annotationInfo = StringUtils.removeIfStartWith(desc, "@").trim();
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
            ctAnnotations.addAnnotation(ctAnnotation);
        }
        return ctAnnotations;
    }


    public static void parseParam2(String syntax, MappingMethodInfo methodInfo){
        MappingMethodInfo.RequestParamInfo paramInfo = parseParam2(syntax);
        methodInfo.addParam(paramInfo);
    }

    public static MappingMethodInfo.RequestParamInfo parseParam2(String syntax){
        syntax = syntax.trim();
        boolean required = syntax.startsWith("!");
        syntax = StringUtils.removeIfStartWith(syntax, "!");
        ParamPart paramPart;
        if (syntax.startsWith("?")){
            paramPart = ParamPart.RequestBody;
        }else if (syntax.startsWith("%")){
            paramPart = ParamPart.RequestPart;
        }else {
            paramPart = ParamPart.RequestParam;
        }
        syntax = StringUtils.removeIfStartWith(syntax, "?");
        syntax = StringUtils.removeIfStartWith(syntax, "%");
        TypeRegister typeRegister = TypeRegister.getInstance();
        String[] paramAndAnnotation = syntax.split("@");
        List<CtAnnotation> ctAnnotations = new ArrayList<>();
        if (paramAndAnnotation.length > 1){
            String annotationInfo = paramAndAnnotation[1];
            CtAnnotations annotations = parseAnnotations(annotationInfo);
            ctAnnotations.addAll(annotations.getAnnotationList());
        }
        String typeGeneric = "";
        String param = paramAndAnnotation[0];
        String[] nameAndType = param.split("::");

        Class<?> type = String.class;
        if (nameAndType.length == 2){
            String typeDesc = nameAndType[1].trim();
            if (typeDesc.contains("<") && typeDesc.endsWith(">")){
                String desc = typeDesc.substring(typeDesc.indexOf("<"));
                typeDesc = typeDesc.substring(0, typeDesc.indexOf("<"));
                GenericInfo info = parseGenericInfo(desc);
                typeGeneric = info.toString();
            }
            type = typeRegister.getType(typeDesc);
        }
        String name = nameAndType[0].trim();
        if (paramPart == ParamPart.RequestBody){
            if (nameAndType.length == 1){
                type = JSONObject.class;
            }
        }
        MappingMethodInfo.RequestParamInfo paramInfo = new MappingMethodInfo.RequestParamInfo(name, type, required, paramPart);
        if (!ctAnnotations.isEmpty()){
            paramInfo.addAnnotations(ctAnnotations.toArray(new CtAnnotation[0]));
        }
        paramInfo.setGenericDesc(typeGeneric);
        return paramInfo;
    }

    //[string, map[string, object]]
    public static GenericInfo parseGenericInfo(String desc){
        GenericInfo info = new GenericInfo();
        if (!StringUtils.hasText(desc)){
            return info;
        }
        desc = StringUtils.addIfNotStartWith(desc, "<");
        desc = StringUtils.addIfNotEndWith(desc, ">");
        TypeRegister register = TypeRegister.getInstance();
        List<BlendObjects> blendObjects = CharParser.parseBlends(desc, '<', '>');
        BlendObjects bo = blendObjects.get(0);
        for (String attribute : bo.getAttributes()) {
            Generic generic = new Generic(register.getType(attribute));
            info.addGeneric(generic);
        }
        for (BlendObjects blendObject : bo.getBlendObjects().values()) {
            String name = blendObject.getName();
            Class<?> type = register.getType(name);
            Generic generic = new Generic(type);
            for (String attribute : blendObject.getAttributes()) {
                generic.addGeneric(new Generic(register.getType(attribute)));
            }
            Collection<BlendObjects> objects = blendObject.getBlendObjects().values();
            for (BlendObjects object : objects) {
                processBlendGeneric(generic, object);
            }
            info.addGeneric(generic);
        }
        return info;
    }

    protected static void processBlendGeneric(Generic parent, BlendObjects blendObject){
        TypeRegister register = TypeRegister.getInstance();
        Generic generic = new Generic(register.getType(blendObject.getName()));
        for (String attribute : blendObject.getAttributes()) {
            generic.addGeneric(new Generic(register.getType(attribute)));
        }
        parent.addGeneric(generic);
        for (BlendObjects bo : blendObject.getBlendObjects().values()) {
            processBlendGeneric(generic, bo);
        }
    }


    public static void main(String[] args) {
        test();
    }

    static void uuid(){
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 1000000; i++) {
            set.add(IdUtils.createId());
        }
        System.out.println(set.size());
    }

    static void test(){
        System.out.println(parseParam2("!?body::map<string, int>@V2Swagger(value:user{})&notnull"));
    }

    static void xml(){
        XmlWrapper xmlWrapper = (XmlWrapper) FactoryManager.initAndGetBeanFactory().get("xml-sql/UserController.xml");
        ElementWrapper element = xmlWrapper.getRootElement();
        XmlServletRegister register = XmlServletRegister.getInstance();
        register.parseWrapper("UserController", element);
    }

}

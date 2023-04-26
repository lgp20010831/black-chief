package com.black.blent;

import com.black.function.Consumer;
import com.black.function.Function;
import com.black.javassist.JavassistCtClassManager;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.utils.IdUtils;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class BlentJavassistManager {

    public static String DEFAULT_CLASS_NAME = "Chief_Invented";


    public static void addClassFields(CtClass ctClass, Class<?> target, Collection<CtField> collection){
        ClassWrapper<?> cw = ClassWrapper.get(target);
        for (FieldWrapper fw : cw.getFields()) {

            Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback = new LinkedHashMap<>();
            Collection<java.lang.annotation.Annotation> annotations = fw.getAnnotations();
            for (java.lang.annotation.Annotation annotation : annotations) {
                AnnotationTypeWrapper typeWrapper = AnnotationTypeWrapper.get(annotation.annotationType());
                Collection<MethodWrapper> methods = typeWrapper.getMethods();
                annotationCallback.put(annotation.annotationType(), amp -> {
                    for (MethodWrapper method : methods) {
                        addMemberValue(method, amp, annotation, typeWrapper);
                    }
                });
            }
            CtField field = Utils.createField(fw.getName(), fw.getType(), annotationCallback, ctClass);
            collection.add(field);
        }
    }

    private static void addMemberValue(MethodWrapper mw,
                                       Annotation annotation,
                                       java.lang.annotation.Annotation javaAnn,
                                       AnnotationTypeWrapper typeWrapper){
        String name = mw.getName();
        Class<?> returnType = mw.getReturnType();
        Object value = typeWrapper.getValue(name, javaAnn);
        MemberValue memberValue = Utils.getMemberValue(returnType, value);
        if (memberValue != null){
            annotation.addMemberValue(name, memberValue);
        }
    }

    public static String getDescByBlent(Blent blent){
        StringJoiner joiner = new StringJoiner("_");
        joiner.add(blent.getBlentDesc());
        for (Blent blentChild : blent.getBlentChilds()) {
            joiner.add(blentChild.getBlentDesc() + "#" + blentChild.getAlias());
        }
        return joiner.toString();
    }

    public static Class<?> parseBlentToClass(Blent blent, Function<String, Class<?>> beanFunction){
        String desc = getDescByBlent(blent);
        Class<?> target = JavassistCtClassManager.tryGetCtClass(desc);
        if (target != null){
            return target;
        }
        PartiallyCtClass partiallyCtClass = doParseBlentToClass(blent, beanFunction);
        target = partiallyCtClass.getJavaClass();
        JavassistCtClassManager.registerJavassistClass(desc, target);
        return target;
    }

    public static PartiallyCtClass parseBlentToPartiallyClass(Blent blent, Function<String, Class<?>> beanFunction){
        return doParseBlentToClass(blent, beanFunction);
    }


    public static PartiallyCtClass doParseBlentToClass(Blent blent, Function<String, Class<?>> beanFunction){
        List<String> planes = blent.getPlanes();
        //创建一个虚拟类
        CtClass ctClass = Utils.createClass(Utils.FICTITIOUS_PATH + "." + DEFAULT_CLASS_NAME +  "_" + IdUtils.createShort8Id());
        PartiallyCtClass partiallyCtClass = new PartiallyCtClass(ctClass);
        Collection<CtField> fields = new ArrayList<>();
        for (String plane : planes) {
            try {
                Class<?> apply = beanFunction.apply(plane);
                Assert.notNull(apply, "bean class is null [" + plane + "]");
                addClassFields(ctClass, apply, fields);
            } catch (Throwable e) {
                throw new IllegalStateException("execute plane to json fair:" + plane, e);
            }
        }
        List<Blent> blentChilds = blent.getBlentChilds();
        for (Blent child : blentChilds) {
            loop(fields, child, beanFunction, ctClass);
        }


        partiallyCtClass.addAllField(fields);
        return partiallyCtClass;
    }
    private static void loop(Collection<CtField> fields, Blent blent, Function<String, Class<?>> beanFunction, CtClass ctClass){
        List<String> planes = blent.getPlanes();
        if (planes.size() == 1){
            String p = planes.get(0);
            try {
                Class<?> apply = beanFunction.apply(p);
                List<Blent> blentChilds = blent.getBlentChilds();
                if (!blentChilds.isEmpty()){
                    List<CtField> currentFields = new ArrayList<>();
                    String className = apply.getSimpleName() + "_" + IdUtils.createShort8Id();
                    PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(className);
                    partiallyCtClass.tranforClass(apply);
                    for (Blent object : blentChilds) {
                        loop(currentFields, object, beanFunction, partiallyCtClass.getCtClass());
                    }
                    partiallyCtClass.addAllField(currentFields);
                    apply = partiallyCtClass.getJavaClass();
                }
                CtField ctField;
                String alias = blent.getAlias();
                if (!StringUtils.hasText(alias)){
                    alias = blent.isJson() ? p : p + "List";
                }
                if (blent.isJson()){
                    ctField = Utils.createField(alias, apply, null, ctClass);
                }else {
                    ctField = Utils.createField(alias, List.class, null, ctClass);
                    Utils.getGenericSignature(ctField, apply, "List");
                }
               fields.add(ctField);
            } catch (Throwable e) {
                throw new IllegalStateException("execute plane to json fair:" + p, e);
            }

        }else {
            throw new IllegalStateException("loop plane must = 1");
        }
    }

}

package com.black.javassist;

import com.alibaba.fastjson.JSONObject;
import com.black.function.Consumer;
import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.generic.Generic;
import com.black.generic.GenericInfo;
import com.black.utils.ServiceUtils;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("all")
public class Utils {

    private static ClassPool pool;

    public static String FICTITIOUS_PATH = "com.black.fictitious";

    public static final Class<?> DEFAULT_FIELD_TYPE = String.class;

    static {
        pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    public static Map<String, CtClass> getPoolCache(){
        try {
            Field field = ClassPool.class.getDeclaredField("classes");
            field.setAccessible(true);
            return (Map<String, CtClass>) field.get(getPool());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static ClassPool getPool() {
        return pool;
    }

    public interface JsonFieldCallBack{
        void callback(CtField field, CtClass ctClass, Object jsonValue) throws Throwable;
    }

    public static void tranfor(CtClass ctClass, Class<?> javaClass, Collection<CtField> fields){
        ClassWrapper<?> cw = ClassWrapper.get(javaClass);
        for (FieldWrapper fw : cw.getFields()) {
            CtField ctField = createField(fw, ctClass);
            fields.add(ctField);
        }
    }

    public static void addAnnotationOnField(CtField field, CtClass ctClass,
                                            Class<? extends java.lang.annotation.Annotation> type,
                                            String methodName, String value) throws NotFoundException {
        ConstPool constPool = ctClass.getClassFile().getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation ann = new Annotation(type.getName(), constPool);
        ann.addMemberValue(methodName, new StringMemberValue(value, getPool().get(String.class.getName()).getClassFile().getConstPool()));
        attr.addAnnotation(ann);
        field.getFieldInfo().addAttribute(attr);
    }

    public static List<CtField> mutateJsonToFields(JSONObject json, CtClass ctClass, JsonFieldCallBack callBack){
        List<CtField> list = new ArrayList<>();
        for (String key : json.keySet()) {
            Object value = json.get(key);
            Class<?> type = value == null ? DEFAULT_FIELD_TYPE : BeanUtil.getPrimordialClass(value);
            try {
                CtClass fieldClass = simpleFieldType(type);
                CtField ctField = new CtField(fieldClass, key, ctClass);
                ctField.setModifiers(Modifier.PUBLIC);
                ConstPool constPool = ctClass.getClassFile().getConstPool();
                if (callBack != null){
                    callBack.callback(ctField, ctClass, value);
                }
                list.add(ctField);
            }catch (Throwable e){
                throw new IllegalStateException(e);
            }

        }
        return list;
    }

    public static void addAnnotationOnClass(CtClass ctClass, Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback){
        try {
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            if (annotationCallback != null){
                AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Class<? extends java.lang.annotation.Annotation> at : annotationCallback.keySet()) {
                    Annotation ann = new Annotation(at.getName(), constPool);
                    Consumer<Annotation> consumer = annotationCallback.get(at);
                    if (consumer != null)
                        consumer.accept(ann);
                    attr.addAnnotation(ann);
                }
                ClassFile classFile = ctClass.getClassFile();
                ctClass.getClassFile().addAttribute(attr);
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }

    }

    public static void addAnnotationOnMethod(CtMethod ctMethod, CtClass ctClass, Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback){
        try {
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            if (annotationCallback != null){
                AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Class<? extends java.lang.annotation.Annotation> at : annotationCallback.keySet()) {
                    Annotation ann = new Annotation(at.getName(), constPool);
                    Consumer<Annotation> consumer = annotationCallback.get(at);
                    if (consumer != null)
                        consumer.accept(ann);
                    attr.addAnnotation(ann);
                }
                ctMethod.getMethodInfo().addAttribute(attr);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }

    }




    public static void addAnnotationToParameter(
            final CtClass ctClass, // the class with the method
            final CtMethod method, // the method with the targeted parameter
            final int index, // the parameter index
            Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback) // to change annotation values
            {
                try {
                    // get parameter annotations and create them if not present
                    final MethodInfo methodInfo = method.getMethodInfo();
                    final ConstPool constPool = methodInfo.getConstPool();
                    ParameterAnnotationsAttribute attribute =
                            (ParameterAnnotationsAttribute)
                                    methodInfo.getAttribute(ParameterAnnotationsAttribute.visibleTag);
                    if (attribute == null)
                        attribute = new ParameterAnnotationsAttribute(constPool, ParameterAnnotationsAttribute.visibleTag);
                    // get annotations
                    // index1 :参数下标, index2:参数注解集合
                    Annotation[][] annotations = attribute.getAnnotations();
                    final CtClass[] types = method.getParameterTypes();
                    // 调整注释数组的大小以匹配参数计数
                    if (annotations.length < types.length) {
                        final Annotation[][] actualAnnotations = new Annotation[types.length][];
                        System.arraycopy(annotations, 0, actualAnnotations, 0, annotations.length);
                        annotations = actualAnnotations;
                    }
                    // 为每个不带注释的参数添加一个空数组
                    for (int i = 0; i < annotations.length; i++){
                        if (annotations[i] == null) annotations[i] = new Annotation[0];
                    }

                    Annotation[] parameterAnnotations = annotations[index];


                    if (annotationCallback != null){
                        int start = parameterAnnotations.length;
                        // 展开参数注释阵列以适应新注释
                        parameterAnnotations = Arrays.copyOf(parameterAnnotations, parameterAnnotations.length + annotationCallback.size());
                        int szieIndex = 0;
                        for (Class<? extends java.lang.annotation.Annotation> javaAnn : annotationCallback.keySet()) {
                            szieIndex++;
                            Consumer<Annotation> consumer = annotationCallback.get(javaAnn);
                            // create new annotation
                            final Annotation annotation = new Annotation(javaAnn.getName(), constPool);
                            consumer.accept(annotation);
                            // set annotation
                            parameterAnnotations[start + szieIndex -1] = annotation;
                        }
                        // set parameter annotations
                        annotations[index] = parameterAnnotations;
                        // set parameter annotations to attribute and method info
                        attribute.setAnnotations(annotations);
                        methodInfo.addAttribute(attribute);
                    }
                }catch (Throwable ex){
                    throw new IllegalStateException(ex);
                }

        }

    public static CtClass getClass(String path){
        try {
            return getPool().get(path);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CtClass createInterface(String path){
        return pool.makeInterface(path);
    }

    public static CtClass createClass(String path){
        return createClass(path, null);
    }
    public static CtClass createClass(String path,
                                      Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback){
        try {
            CtClass ctClass = pool.makeClass(path);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            if (annotationCallback != null){
                AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Class<? extends java.lang.annotation.Annotation> at : annotationCallback.keySet()) {
                    Annotation ann = new Annotation(at.getName(), constPool);
                    Consumer<Annotation> consumer = annotationCallback.get(at);
                    if (consumer != null)
                        consumer.accept(ann);
                    attr.addAnnotation(ann);
                }
                ClassFile classFile = ctClass.getClassFile();
                ctClass.getClassFile().addAttribute(attr);
            }

            return ctClass;
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
    }

    public static Class<?> createJavaClass(@NonNull Collection<CtField> fields, CtClass ctClass){
        return createJavaClass(fields, ctClass, true);
    }
    public static Class<?> createJavaClass(@NonNull Collection<CtField> fields, CtClass ctClass, boolean creeateGetAndSet){
        try {
            for (CtField field : fields) {
                String name = field.getName();
                String genericSignature = field.getGenericSignature();
                ctClass.addField(field);
                if (creeateGetAndSet){
                    String suffix = StringUtils.titleCase(name);
                    ctClass.addMethod(CtNewMethod.setter("set" + suffix, field));
                    CtMethod getMethod = CtNewMethod.getter("get" + suffix, field);
                    if (genericSignature != null){
                        getMethod.setGenericSignature(getGetMethodGenericSignature(genericSignature));
                    }
                    ctClass.addMethod(getMethod);
                }
            }

            return ctClass.toClass();
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
    }

    public static String getGetMethodGenericSignature(String genericSignature){
        return "()" + genericSignature;
    }

    public static CtMethod createMethod(String name, Class<?> returnType, String body,
                                        CtClass ctClass,
                                        Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback,
                                        Class<?>... paramTypes){
        try {
            CtClass[] ctClasses = new CtClass[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                ctClasses[i] = simpleFieldType(paramType);
            }
            CtMethod ctMethod = new CtMethod(simpleFieldType(returnType), name, ctClasses, ctClass);
            ctMethod.setBody(body);

            ctMethod.setModifiers(Modifier.PUBLIC);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            if (annotationCallback != null){
                AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Class<? extends java.lang.annotation.Annotation> at : annotationCallback.keySet()) {
                    Annotation ann = new Annotation(at.getName(), constPool);
                    Consumer<Annotation> consumer = annotationCallback.get(at);
                    if (consumer != null)
                        consumer.accept(ann);
                    attr.addAnnotation(ann);
                }
                ctMethod.getMethodInfo().addAttribute(attr);
            }
            return ctMethod;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static ConstPool createConstPool(Class<?> type){
        try {
            return pool.get(type.getName()).getClassFile().getConstPool();
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CtField createField(FieldWrapper fieldWrapper,
                                      CtClass ctClass){
        Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback = new HashMap<>();

        for (java.lang.annotation.Annotation annotation : fieldWrapper.getAnnotations()) {
            AnnotationTypeWrapper atw = AnnotationTypeWrapper.get(annotation.annotationType());
            Consumer<Annotation> consumer = ann -> {
                Map<String, MethodWrapper> annotationMethods = atw.getAnnotationMethods();
                for (String methodName : annotationMethods.keySet()) {
                    MethodWrapper mw = annotationMethods.get(methodName);
                    Class<?> returnType = mw.getReturnType();
                    Object value = atw.getValue(methodName, annotation);
                    MemberValue memberValue = getMemberValue(returnType, value);
                    if (memberValue != null){
                        ann.addMemberValue(methodName, memberValue);
                    }
                }
            };
            annotationCallback.put(annotation.annotationType(), consumer);
        }
        return createField(fieldWrapper.getName(), fieldWrapper.getType(), annotationCallback, ctClass);
    }

    public static CtField createField(String name, Class<?> type,
                                      Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annotationCallback,
                                      CtClass ctClass){
        try {
            CtClass fieldClass = simpleFieldType(type);
            CtField ctField = new CtField(fieldClass, name, ctClass);
            ctField.setModifiers(Modifier.PUBLIC);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            if (annotationCallback != null){
                AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                for (Class<? extends java.lang.annotation.Annotation> at : annotationCallback.keySet()) {
                    Annotation ann = new Annotation(at.getName(), constPool);
                    Consumer<Annotation> consumer = annotationCallback.get(at);
                    if (consumer != null)
                        consumer.accept(ann);
                    attr.addAnnotation(ann);
                }
                ctField.getFieldInfo().addAttribute(attr);
            }
            return ctField;
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
    }

    /**
     * 创建类的属性
     * @param key
     * @param dataType
     * @param responseContainer
     * @param description
     * @param ctClass
     * @return
     * @throws Exception
     */
    public static CtField createField(String key, Class<?> dataType, String responseContainer,
                                String description,
                                CtClass ctClass) throws Exception {
        CtClass fieldClass = getFieldType(dataType, responseContainer);
        CtField ctField = new CtField(fieldClass, key, ctClass);
        if(StringUtils.hasText(responseContainer)){
            getGenericSignature(ctField,dataType,responseContainer);
        }
        ctField.setModifiers(Modifier.PUBLIC);
        ConstPool constPool = ctClass.getClassFile().getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation ann = new Annotation("io.swagger.annotations.ApiModelProperty", constPool);
        ann.addMemberValue("value", new StringMemberValue(description, constPool));
     /*   if(ctField.getType().subclassOf(ClassPool.getDefault().get(String.class.getName()))){
            ann.addMemberValue("example", new StringMemberValue(example, ClassPool.getDefault().get(String.class.getName()).getClassFile().getConstPool()));
        }
        if(ctField.getType().subclassOf(ClassPool.getDefault().get(Integer.class.getName()))){
            ann.addMemberValue("example", new IntegerMemberValue(Integer.parseInt(example), ClassPool.getDefault().get(Integer.class.getName()).getClassFile().getConstPool()));
        }
        if(ctField.getType().subclassOf(ClassPool.getDefault().get(Boolean.class.getName()))){
            ann.addMemberValue("example", new BooleanMemberValue(Boolean.parseBoolean(example), ClassPool.getDefault().get(Boolean.class.getName()).getClassFile().getConstPool()));
        }*/
        attr.addAnnotation(ann);
        ctField.getFieldInfo().addAttribute(attr);
        return ctField;
    }

    public static CtClass simpleFieldType(Class<?> classObj) throws NotFoundException {
        return pool.get(classObj.getName());
    }


    public static CtClass getAndCreateClass(Class clazz){
        try {
            return pool.get(clazz.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static CtClass getAndCreateClass(String className){
        try {
            return pool.get(className);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 生成返回对象的属性class
     * @param classObj
     * @param responseContainer
     * @return
     * @throws Exception
     */
    public static CtClass getFieldType(Class<?> classObj,String responseContainer) throws Exception {
        if ("List".compareToIgnoreCase(responseContainer) == 0) {
            return pool.get(List.class.getCanonicalName());
        } else if ("Set".compareToIgnoreCase(responseContainer) == 0) {
            return pool.get(Set.class.getCanonicalName());
        }
        return pool.get(classObj.getName());
    }

    public static void setFieldGeneric(CtField field, GenericInfo genericInfo){
        try {
            if (genericInfo != null){
                String name = field.getType().getName().replace(".", "/");
                String desc = "L" + name + (genericInfo == null ? "" : genericInfo.toString());
                field.setGenericSignature(SignatureAttribute.toClassSignature(desc).encode());
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *  javasist对CtField的泛型类型添加泛型的类声明
     * @param ctField
     * @param relatedClass
     * @param responseContainer
     * @return
     * @throws BadBytecode
     */
    public static CtField getGenericSignature(CtField ctField, Class<?> relatedClass,String responseContainer) throws BadBytecode {
            String fieldSignature = "";
        if ("List".compareToIgnoreCase(responseContainer) == 0) {
                fieldSignature = "L" + List.class.getCanonicalName().replace(".", "/") + "<L" + relatedClass.getCanonicalName().replace(".", "/") + ";>;";
        } else if ("Set".compareToIgnoreCase(responseContainer) == 0) {
                fieldSignature = "L" + Set.class.getCanonicalName().replace(".", "/") + "<L" + relatedClass.getCanonicalName().replace(".", "/") + ";>;";
        }else {
            return ctField;
        }
        ctField.setGenericSignature(SignatureAttribute.toClassSignature(fieldSignature).encode());
        return ctField;
    }

    public static String getFieldSignature(Class<?> relatedClass,String responseContainer){
        String fieldSignature = "";
        if ("List".compareToIgnoreCase(responseContainer) == 0) {
            fieldSignature = "L" + List.class.getCanonicalName().replace(".", "/") + "<L" + relatedClass.getCanonicalName().replace(".", "/") + ";>;";
        } else if ("Set".compareToIgnoreCase(responseContainer) == 0) {
            fieldSignature = "L" + Set.class.getCanonicalName().replace(".", "/") + "<L" + relatedClass.getCanonicalName().replace(".", "/") + ";>;";
        }
        return fieldSignature;
    }

    public static MemberValue getMemberValue(Class<?> type, Object value){
        MemberValue memberValue = null;
        if (type.equals(String.class)){
            memberValue = new StringMemberValue(String.valueOf(value), Utils.createConstPool(String.class));
        }else if (type.equals(int.class)){
            memberValue = new IntegerMemberValue(Utils.createConstPool(Integer.class), Integer.parseInt(value.toString()));
        }else if (type.equals(Class.class)){
            Class<?> clazz = (Class<?>) value;
            memberValue = new ClassMemberValue(clazz.getName(), Utils.createConstPool(Class.class));
        }else if (type.equals(String[].class)){
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(Utils.createConstPool(String.class));
            List<Object> list = SQLUtils.wrapList(value);
            List<MemberValue> memberValues = new ArrayList<>();
            for (Object val : list) {
                memberValues.add(new StringMemberValue(String.valueOf(val), Utils.createConstPool(String.class)));
            }
            arrayMemberValue.setValue(memberValues.toArray(new MemberValue[0]));
            memberValue = arrayMemberValue;
        }else if (type.equals(boolean.class)){
            memberValue = new BooleanMemberValue(Boolean.parseBoolean(String.valueOf(value)), Utils.createConstPool(Boolean.class));
        }else if (type.equals(int[].class)){
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(Utils.createConstPool(Integer.class));
            List<Object> list = SQLUtils.wrapList(value);
            List<MemberValue> memberValues = new ArrayList<>();
            for (Object val : list) {
                memberValues.add(new IntegerMemberValue(Utils.createConstPool(Integer.class), Integer.parseInt(String.valueOf(val))));
            }
            arrayMemberValue.setValue(memberValues.toArray(new MemberValue[0]));
            memberValue = arrayMemberValue;
        }else if (type.equals(Class[].class)){
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(Utils.createConstPool(Class.class));
            List<Object> list = SQLUtils.wrapList(value);
            List<MemberValue> memberValues = new ArrayList<>();
            for (Object val : list) {
                Class<?> clazz = (Class<?>) val;
                memberValues.add(new ClassMemberValue(clazz.getName(), Utils.createConstPool(Class.class)));
            }
            arrayMemberValue.setValue(memberValues.toArray(new MemberValue[0]));
            memberValue = arrayMemberValue;
        }else if (type.isEnum()){
            EnumMemberValue enumMemberValue = new EnumMemberValue(Utils.createConstPool(type));
            enumMemberValue.setType(type.getName());
            enumMemberValue.setValue(((Enum)value).name());
            memberValue = enumMemberValue;
        }else if (type.isArray()){
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(Utils.createConstPool(type.getComponentType()));
            List<Object> list = SQLUtils.wrapList(value);
            List<MemberValue> memberValues = new ArrayList<>();
            for (Object val : list) {
                Class<?> valClass;
                if (val instanceof java.lang.annotation.Annotation){
                    valClass = ((java.lang.annotation.Annotation) val).annotationType();
                }else {
                    valClass = val.getClass();
                }
                memberValues.add(getMemberValue(valClass, val));
            }
            arrayMemberValue.setValue(memberValues.toArray(new MemberValue[0]));
            memberValue = arrayMemberValue;
        }else if (type.isAnnotation()){
            java.lang.annotation.Annotation javaAnn = (java.lang.annotation.Annotation) value;
            Class<? extends java.lang.annotation.Annotation> annotationType = javaAnn.annotationType();
            ConstPool constPool = createConstPool(annotationType);
            Annotation annotation = new Annotation(annotationType.getName(), constPool);
            memberValue = new AnnotationMemberValue(annotation, constPool);
        }else {
            throw new IllegalStateException("不支持类型: " + type + " 的member value");
        }
        return memberValue;
    }



    public static CtClass[] castJavaToCtClassArray(Class<?>... javaClasses){

        CtClass[] ctClasses = new CtClass[javaClasses.length];
        for (int i = 0; i < javaClasses.length; i++) {
            Class<?> clazz = javaClasses[i];
            CtClass ctClass = getAndCreateClass(clazz);
            ctClasses[i] = ctClass;
        }
        return ctClasses;
    }

    public static void addGenericToParam( CtMethod method, // the method with the targeted parameter
                                          String... desc){
        final MethodInfo methodInfo = method.getMethodInfo();
        final ConstPool constPool = methodInfo.getConstPool();
        String descriptor = methodInfo.getDescriptor();
        String genericDesc = ServiceUtils.parseTxt(descriptor, "(", ")", paramDesc -> {
            String[] params = paramDesc.split(";");
            StringJoiner joiner = new StringJoiner("", "(", ")");
            for (int i = 0; i < params.length; i++) {
                String param = params[i];
                if (!StringUtils.hasText(param)) continue;
                if (!param.startsWith("L")) continue;
                if(i > desc.length - 1){
                    joiner.add(param + ";");
                }else {
                    String d = desc[i];
                    if (!StringUtils.hasText(d)){
                        joiner.add(param + ";");
                    }else {
                        param = param + d;
                        joiner.add(param);
                    }
                }
            }
            return joiner.toString();
        });

        try {
            method.setGenericSignature(SignatureAttribute.toMethodSignature(genericDesc).encode());
        } catch (BadBytecode e) {
            throw new IllegalStateException(e);
        }
    }

    public static void addGenericToMethod(CtMethod ctMethod, String desc){
        String descriptor = ctMethod.getGenericSignature();
        int end = descriptor.lastIndexOf(")");
        String returnDesc = descriptor.substring(end + 1);
        returnDesc = StringUtils.removeIfEndWith(returnDesc, ";");
        returnDesc = returnDesc + (StringUtils.hasText(desc) ? desc : ";");
        String complete = descriptor.substring(0, end + 1) + returnDesc;
        try {
            ctMethod.setGenericSignature(SignatureAttribute.toMethodSignature(complete).encode());
        } catch (BadBytecode e) {
            throw new IllegalStateException(e);
        }
    }

    public static void test(List<String> l){}

    public static void main(String[] args) throws BadBytecode {
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(Utils.class);
        GenericInfo info = GenericInfo.group(Generic.of(String.class));
        PartiallyCtClass test = PartiallyCtClass.make("TEST");
        CtMethod method = test.addMethod("hello", List.class, "{return null;}", List.class, List.class);

        addGenericToParam(method, info.toString(), info.toString());
        addGenericToMethod(method, info.toString());
        System.out.println(method);
        Class<?> javaClass = test.getJavaClass();
        ClassWrapper<?> classWrapper = ClassWrapper.get(javaClass);
        System.out.println(classWrapper);
    }
}

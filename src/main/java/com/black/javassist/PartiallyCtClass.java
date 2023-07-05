package com.black.javassist;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.function.Consumer;
import com.black.generic.GenericInfo;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.throwable.IOSException;
import com.black.utils.ServiceUtils;
import javassist.*;
import javassist.bytecode.SignatureAttribute;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
@Getter @Setter
//半成品的虚拟类
public class PartiallyCtClass {

    private static final IoLog log = LogFactory.getLog4j();

    private final ClassPool pool;

    private CtClass ctClass;

    private Class<?> javaClass;

    private CtClass parent;

    private boolean immediately = true;

    private final Collection<CtField> fields = new ArrayList<>();

    private final Map<String, CtMethod> methods = new ConcurrentHashMap<>();

    private final Set<String> fieldNames = new HashSet<>();

    private final Map<String, Class<?>> dependencies = new ConcurrentHashMap<>();

    private final Set<Class<?>> interfaceSet = new HashSet<>();

    public static PartiallyCtClass load(String classPath){
        return new PartiallyCtClass(Utils.getClass(classPath));
    }

    public static PartiallyCtClass make(String className){
        return make(className, Utils.FICTITIOUS_PATH);
    }

    public static PartiallyCtClass make(String className, String path){
        log.info("PartiallyCtClass make class: {}", className);
        return new PartiallyCtClass(Utils.createClass(path + "." + className));
    }

    public static PartiallyCtClass face(String className){
        return face(className, Utils.FICTITIOUS_PATH);
    }

    public static PartiallyCtClass face(String className, String path){
        log.info("PartiallyCtClass make interface: {}", className);
        return new PartiallyCtClass(Utils.createInterface(path + "." + className));
    }

    public Set<Class<? extends Annotation>> getAnnptationTypes(){
        try {
            Object[] annotations = getCtClass().getAnnotations();
            Set<Class<? extends Annotation>> types = new HashSet<>();
            for (Object annotation : annotations) {
                types.add(((Annotation) annotation).annotationType());
            }
            return types;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    public CtConstructor addConstructor(String body, Class<?>... types){
        CtClass[] ctClasses = Utils.castJavaToCtClassArray(types);
        CtConstructor constructor = new CtConstructor(ctClasses, ctClass);
        try {
            constructor.setBody(body);
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
        return constructor;
    }

    public void setSuperClassGenericity(Class<?> superClass, Class<?>... genericityTypes){
        StringJoiner joiner = new StringJoiner(",", superClass.getSimpleName() + "<", ">");
        for (Class<?> genericityType : genericityTypes) {
            joiner.add(genericityType.getName());
        }
        SignatureAttribute.TypeVariable typeVariable = new SignatureAttribute.TypeVariable(joiner.toString());
        getCtClass().setGenericSignature(typeVariable.encode());
        CtConstructor ctor = new CtConstructor( new CtClass[]{}, getCtClass());
        try {
            getCtClass().addConstructor(ctor);
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setInterfaceGenericity(Class<?> superClass, Class<?>... genericityTypes){
        SignatureAttribute.TypeArgument[] typeArguments = new SignatureAttribute.TypeArgument[genericityTypes.length];
        for (int i = 0; i < genericityTypes.length; i++) {
            Class<?> genericityType = genericityTypes[i];
            typeArguments[i] = new SignatureAttribute.TypeArgument(new SignatureAttribute.ClassType(genericityType.getName()));
        }
        SignatureAttribute.ClassSignature ac = new SignatureAttribute.ClassSignature(null, null,
                // Set interface and its generic params
                new SignatureAttribute.ClassType[]{new SignatureAttribute.ClassType(superClass.getName(),
                        typeArguments
                )});

        getCtClass().setGenericSignature(ac.encode());
    }

    public PartiallyCtClass(CtClass ctClass) {
        this.ctClass = ctClass;
        pool = Utils.getPool();
    }

    private void check(){
        if (javaClass != null){
            throw new IllegalStateException("Currently, the design has been completed");
        }
    }

    public CtMethod getLoadMethod(String name, Class<?>... types){
        try {
            return ctClass.getDeclaredMethod(name, Utils.castJavaToCtClassArray(types));
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setSuperClass(Class<?> type){
        check();
        try {
            ctClass.setSuperclass(pool.get(type.getName()));
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void addInterface(Class<?>... classes){
        interfaceSet.addAll(Arrays.asList(classes));
    }

    public void scanDependencies(String range){
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<Class<?>> classSet = scanner.load(range);
        addDependencies(classSet.toArray(new Class[0]));
    }

    public void addDependencies(Class<?>... classes){
        for (Class<?> clazz : classes) {
            String simpleName = clazz.getSimpleName();
            if (dependencies.containsKey(simpleName)){
                log.error("Dependency Repetition: {}", simpleName);
            }
            dependencies.put(simpleName, clazz);
        }
    }

    public void createField(String name, Class<?> type, CtAnnotations annotations, GenericInfo genericInfo){
        createField(name, type, annotations, genericInfo, true);
    }

    public void createField(String name, Class<?> type, CtAnnotations annotations, GenericInfo genericInfo, boolean createGetAndSet){
        CtField ctField = Utils.createField(name, type, annotations == null ? null :
                annotations.getAnnotationCallback(), ctClass);
        Utils.setFieldGeneric(ctField, genericInfo);
        addField(ctField, createGetAndSet);
    }

    public void addField(CtField field){
        addField(field, true);
    }

    public void addField(CtField field, boolean createGetAndSet){
        check();
        if (field != null){
            synchronized (fieldNames){
                String name = field.getName();
                if (!fieldNames.contains(name)){
                    fieldNames.add(name);
                    fields.add(field);
                    try {
                        ctClass.addField(field);
                        if (createGetAndSet){
                            String genericSignature = field.getGenericSignature();
                            String suffix = StringUtils.titleCase(name);
                            ctClass.addMethod(CtNewMethod.setter("set" + suffix, field));
                            CtMethod getMethod = CtNewMethod.getter("get" + suffix, field);
                            if (genericSignature != null){
                                getMethod.setGenericSignature(Utils.getGetMethodGenericSignature(genericSignature));
                            }
                            ctClass.addMethod(getMethod);
                        }
                    } catch (CannotCompileException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }

    public CtMethod[] getRawMethods(){
        return ctClass.getDeclaredMethods();
    }

    public void addMethod(CtMethod method){
        if (method != null){
            methods.put(method.getName(), method);
        }
    }

    public void writeFile(){
        try {
            ctClass.writeFile();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] toByteArray(){
        try {
            return ctClass.toBytecode();
        } catch (Throwable e) {
            throw new IOSException(e);
        }
    }

    public void tranforClass(Class<?> type){
        Collection<CtField> tempFields = new ArrayList<>();
        Utils.tranfor(ctClass, type, tempFields);
        addAllField(tempFields);
    }

    public void addAllField(@NonNull Collection<CtField> fields){
        fields.forEach(this::addField);
    }

    public boolean existFieldName(String name){
        boolean contains = fieldNames.contains(name);
        if (!contains){
            try {
                 ctClass.getField(name);
                return true;
            } catch (NotFoundException e) {
                return false;
            }
        }
        return contains;
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    public Collection<CtField> getFields() {
        return fields;
    }

    public void addClassAnnotations(CtAnnotations annotations){
        check();
        Map<Class<? extends Annotation>, Consumer<javassist.bytecode.annotation.Annotation>> callback =
                annotations.getAnnotationCallback();
        Utils.addAnnotationOnClass(ctClass, callback);
    }

    public void addMethodAnnotations(String name, CtAnnotations annotations){
        check();
        Map<Class<? extends Annotation>, Consumer<javassist.bytecode.annotation.Annotation>> callback =
                annotations.getAnnotationCallback();
        CtMethod ctMethod = methods.get(name);
        Assert.notNull(ctMethod, "can not find method:" + name);
        Utils.addAnnotationOnMethod(ctMethod, ctClass, callback);
    }

    public synchronized Class<?> getJavaClass(){
        if (javaClass != null){
            return javaClass;
        }
        for (CtMethod ctMethod : methods.values()) {
            try {
                ctClass.addMethod(ctMethod);
            } catch (CannotCompileException e) {
                throw new IllegalStateException(e);
            }
        }
        Set<CtClass> ctClasses = StreamUtils.mapSet(interfaceSet, type -> {
            try {
                return pool.get(type.getName());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });
        ctClass.setInterfaces(ctClasses.toArray(new CtClass[0]));
        try {
            javaClass = ctClass.toClass();
            return javaClass;
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
    }


    public CtMethod addMethod(String name, Class<?> returnType, String body, Class<?>... paramTypes){
        check();
        String prepareBody = prepareBody(body);
        CtMethod ctMethod = Utils.createMethod(name, returnType, prepareBody, ctClass, null, paramTypes);
        addMethod(ctMethod);
        return ctMethod;
    }

    public void setMethodThrowTypes(String name, Class<? extends Throwable>... types){
        check();
        CtClass[] ctClasses = new CtClass[types.length];
        for (int i = 0; i < types.length; i++) {
            ctClasses[i] = Utils.getAndCreateClass(types[i]);
        }
        CtMethod ctMethod = getMethod(name);
        try {
            ctMethod.setExceptionTypes(ctClasses);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean containMethod(String name){
        boolean containsed = methods.containsKey(name);
        if (!containsed){
            try {
                ctClass.getDeclaredMethod(name);
                return true;
            } catch (NotFoundException e) {
                return false;
            }
        }
        return containsed;
    }

    public CtMethod getMethod(String name){
        CtMethod ctMethod = methods.get(name);
        if (ctMethod == null){
            try {
                ctMethod = ctClass.getDeclaredMethod(name);
            } catch (NotFoundException e) {
                throw new IllegalStateException("not found method:" + name);
            }
        }
        return ctMethod;
    }

    public void addParameterAnnotation(String methodName, int index, CtAnnotations annotations){
        check();
        CtMethod ctMethod = getMethod(methodName);
        Utils.addAnnotationToParameter(ctClass, ctMethod, index, annotations.getAnnotationCallback());
    }

    public void addParamGeneric(String methodName, String... descs){
        check();
        CtMethod method = getMethod(methodName);
        Utils.addGenericToParam(method, descs);
    }

    public void addMethodReturnGeneric(String methodName, GenericInfo genericInfo){
        check();
        addMethodReturnGeneric(methodName, genericInfo.toString());
    }

    public void addMethodReturnGeneric(String methodName, String info){
        check();
        CtMethod method = getMethod(methodName);
        Utils.addGenericToMethod(method, info);
    }

    private String prepareBody(String body){
        return ServiceUtils.parseTxt(body, "#{", "}", name -> {
            Class<?> type = dependencies.get(name);
            if (type == null){
                throw new IllegalStateException("can not find dependency: " + name);
            }
            return type.getName();
        });
    }
}

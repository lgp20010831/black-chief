package com.black.javassist;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.function.Consumer;
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

@Getter @Setter
//半成品的虚拟类
public class PartiallyCtClass {

    private static final IoLog log = LogFactory.getLog4j();

    private final ClassPool pool;

    private CtClass ctClass;

    private CtClass parent;


    private final Collection<CtField> fields = new ArrayList<>();

    private final Map<String, CtMethod> methods = new ConcurrentHashMap<>();

    private final Set<String> fieldNames = new HashSet<>();

    private final Map<String, Class<?>> dependencies = new ConcurrentHashMap<>();

    private final Set<Class<?>> interfaceSet = new HashSet<>();

    public static PartiallyCtClass load(String classPath){
        return new PartiallyCtClass(Utils.getClass(classPath));
    }

    public static PartiallyCtClass load(Class<?> type){
        return new PartiallyCtClass(Utils.getClass(type.getName()));
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


    public CtMethod getLoadMethod(String name, Class<?>... types){
        try {
            return ctClass.getDeclaredMethod(name, Utils.castJavaToCtClassArray(types));
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setSuperClass(Class<?> type){
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


    public void addField(CtField field){
        if (field != null){
            synchronized (fieldNames){
                String name = field.getName();
                if (!fieldNames.contains(name)){
                    fieldNames.add(name);
                    fields.add(field);
                }
            }
        }
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
        return fieldNames.contains(name);
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    public Collection<CtField> getFields() {
        return fields;
    }

    public void addClassAnnotations(CtAnnotations annotations){
        Map<Class<? extends Annotation>, Consumer<javassist.bytecode.annotation.Annotation>> callback =
                annotations.getAnnotationCallback();
        Utils.addAnnotationOnClass(ctClass, callback);
    }

    public void addMethodAnnotations(String name, CtAnnotations annotations){
        Map<Class<? extends Annotation>, Consumer<javassist.bytecode.annotation.Annotation>> callback =
                annotations.getAnnotationCallback();
        CtMethod ctMethod = methods.get(name);
        Assert.notNull(ctMethod, "can not find method:" + name);
        Utils.addAnnotationOnMethod(ctMethod, ctClass, callback);
    }

    public Class<?> getJavaClass(){
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
        return Utils.createJavaClass(fields, ctClass, true);
    }


    public void addMethod(String name, Class<?> returnType, String body, Class<?>... paramTypes){
        String prepareBody = prepareBody(body);
        CtMethod ctMethod = Utils.createMethod(name, returnType, prepareBody, ctClass, null, paramTypes);
        addMethod(ctMethod);
    }

    public boolean containMethod(String name){
        return methods.containsKey(name);
    }

    public CtMethod getMethod(String name){
        CtMethod ctMethod = methods.get(name);
        Assert.notNull(ctMethod, "not find method:"  + name);
        return ctMethod;
    }

    public void addParameterAnnotation(String methodName, int index, CtAnnotations annotations){
        CtMethod ctMethod = getMethod(methodName);
        Utils.addAnnotationToParameter(ctClass, ctMethod, index, annotations.getAnnotationCallback());
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

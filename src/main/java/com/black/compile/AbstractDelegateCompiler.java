package com.black.compile;

import com.alibaba.fastjson.JSONObject;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.json.JsonUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.util.StringUtils;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.Map;

@SuppressWarnings("all")
public abstract class AbstractDelegateCompiler implements DelegateCompiler{

    protected PartiallyCtClass partiallyCtClass;

    protected Class<?> generateClass;

    public static final String COMPILER_CLASS_PREFIX = "DelegateGenerate";

    protected boolean asyn = false;

    protected boolean autoClear = true;

    protected boolean markEscape = true;

    @Override
    public void setMarkEscape(boolean markEscape) {
        this.markEscape = markEscape;
    }

    @Override
    public boolean isMarkEscape() {
        return markEscape;
    }

    @Override
    public void setAsyn(boolean asyn) {
        this.asyn = asyn;
    }

    @Override
    public boolean isAsyn() {
        return asyn;
    }

    @Override
    public String getEntry() {
        return "run";
    }

    @Override
    public String getParamName() {
        return "$1";
    }

    @Override
    public void run(Object param) {
        if (partiallyCtClass == null){
            throw new IllegalStateException("The current class is not compiled");
        }

        if (generateClass == null){
            generateClass = partiallyCtClass.getJavaClass();
        }

        try {
            ClassWrapper<?> classWrapper = ClassWrapper.get(generateClass);
            Object instance = InstanceBeanManager.instance(generateClass, InstanceType.REFLEX_AND_BEAN_FACTORY);
            JSONObject jsonParam = JsonUtils.letJson(param);
            run0(classWrapper, instance, jsonParam, isAsyn());
        }finally {
            if (isAutoClear()){
                clear();
            }
        }
    }

    protected void run0(ClassWrapper<?> classWrapper, Object instance, Map<String, Object> param, boolean asyn){
        if (asyn){
            AsynGlobalExecutor.execute(() -> {
                classWrapper.getSingleMethod(getEntry()).invoke(instance, param);
            });
        }else {
            classWrapper.getSingleMethod(getEntry()).invoke(instance, param);
        }
    }

    @Override
    public void compile(String code) {
        check();
        code = prepareCode(code);
        CtMethod method = createAndAddMethod(code);
        postMethod(method);
    }

    protected void postMethod(CtMethod method){

    }

    protected CtMethod createAndAddMethod(String code){
        CtMethod method = partiallyCtClass.addMethod(getEntry(), void.class, code, Map.class);
        try {
            method.setExceptionTypes(new CtClass[]{Utils.getAndCreateClass(Throwable.class)});
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
        return method;
    }

    protected void check(){
        if (partiallyCtClass == null){
            partiallyCtClass = PartiallyCtClass.make(COMPILER_CLASS_PREFIX + IdUtils.createShort8Id());
        }

        if (generateClass != null){
            throw new IllegalStateException("The current class is already compiled");
        }
    }

    protected String prepareCode(String code){
        if (!StringUtils.hasText(code)){
            return "{}";
        }

        code = StringUtils.addIfNotStartWith(code, "{");
        code = StringUtils.addIfNotEndWith(code, "}");
        code = ServiceUtils.parseTxt(code, "$[", "]", imports -> {
            String[] splits = imports.split(",");
            for (String split : splits) {
                importDependencyPackage(split.trim());
            }
            return "";
        });
        if (isMarkEscape()){
            code = ServiceUtils.parseTxt(code, "${", "}", txt -> {
                return "\"" + txt + "\"";
            });
        }
        return code;
    }

    @Override
    public void setAutoClear(boolean autoClear) {
        this.autoClear = autoClear;
    }

    @Override
    public boolean isAutoClear() {
        return autoClear;
    }

    @Override
    public void clear() {
        generateClass = null;
        partiallyCtClass = null;
    }

    @Override
    public void importDependencyPackage(String... packages) {
        ClassPool pool = Utils.getPool();
        for (String name : packages) {
            pool.importPackage(name);
        }
    }
}

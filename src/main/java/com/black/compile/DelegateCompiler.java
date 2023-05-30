package com.black.compile;

@SuppressWarnings("all")
public interface DelegateCompiler {

    default DelegateCompiler getDelegate(){
        return null;
    }

    default void setAsyn(boolean asyn){
        getDelegate().setAsyn(asyn);
    }

    default boolean isAsyn(){
        return getDelegate().isAsyn();
    }

    default void setMarkEscape(boolean escape){
        getDelegate().setMarkEscape(escape);
    }

    default boolean isMarkEscape(){
        return getDelegate().isMarkEscape();
    }

    default String getEntry(){
        return getDelegate().getEntry();
    }


    default String getParamName(){
        return getDelegate().getParamName();
    }

    default void run(Object param){
        getDelegate().run(param);
    }

    default void compile(String code){
        getDelegate().compile(code);
    }

    default void compileAndRun(String code){
        compileAndRun(code, null);
    }

    default void compileAndRun(String code, Object param){
        compile(code);
        run(param);
    }

    default void setAutoClear(boolean autoClear){
        getDelegate().setAutoClear(autoClear);
    }

    default boolean isAutoClear(){
        return getDelegate().isAutoClear();
    }

    default void clear(){
        getDelegate().clear();
    }

    default void importDependencyPackage(String... packages){
        getDelegate().importDependencyPackage(packages);
    }

}

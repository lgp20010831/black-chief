package com.black.scan;

import com.black.core.cache.ClassSourceCache;

import java.util.Set;

public abstract class AbstractChiefScanner implements ChiefScanner{

    private ClassLoader classLoader;

    private boolean stopOnClassNotFind;

    private boolean printNotFindClass;

    @Override
    public Set<Class<?>> load(String classpath) {
        Set<Class<?>> source = ClassSourceCache.getSource(classpath);
        if (source == null){
            source = doLoad(classpath);
            ClassSourceCache.registerSource(classpath, source);
        }
        return source;
    }

    protected abstract Set<Class<?>> doLoad(String classpath);

    public static String getPackagePath(String packageName) {
        return packageName == null ? null : packageName.replace('.', '/');
    }

    public void setStopOnClassNotFind(boolean stopOnClassNotFind) {
        this.stopOnClassNotFind = stopOnClassNotFind;
    }

    public void setPrintNotFindClass(boolean printNotFindClass) {
        this.printNotFindClass = printNotFindClass;
    }

    public boolean isPrintNotFindClass() {
        return printNotFindClass;
    }

    public boolean isStopOnClassNotFind() {
        return stopOnClassNotFind;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    public static String getClassName(String path){
        String rs;
        return (rs = path.replace('/', '.')).endsWith(".class") ? rs.substring(0, rs.indexOf(".class")) : rs;
    }

}

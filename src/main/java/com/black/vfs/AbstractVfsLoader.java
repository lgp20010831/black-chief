package com.black.vfs;

import com.black.core.cache.ClassScanner;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractVfsLoader implements ClassScanner, VfsScanner {

    private boolean stopOnClassNotFind;

    private boolean printNotFindClass;

    private ClassLoader classLoader;


    //例子：  com.sun.data
    public Set<Class<?>> load(String classPath){
        String packagePath = getPackagePath(classPath);
        Set<Class<?>> classSet = new HashSet<>();
        try {

            List<String> list = listFile(packagePath);
            if (list != null && !list.isEmpty()){
                for (String classUrlPath : list) {
                    if (!classUrlPath.endsWith(".class")){
                        continue;
                    }
                    String cn = getClassName(classUrlPath);
                    try {
                        classSet.add(getClassLoader().loadClass(cn));
                    }catch (Throwable ce){
                        if (isStopOnClassNotFind()){
                            throw new VfsLoadException(ce);
                        }else {
                            if (isPrintNotFindClass())
                                System.out.println("not find class is [" + cn + "]");
                        }
                    }

                }
            }
        } catch (IOException e) {
            throw new VfsLoadException(e);
        }
        return classSet;
    }

    protected abstract List<String> listFile(String packagePath) throws IOException;

    @Override
    public List<String> fileNameList(String scanPath) {
        try {
            return listFile(getPackagePath(scanPath));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

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

    @Override
    public Set<Class<?>> scan(String packageName) {
        return load(packageName);
    }



}

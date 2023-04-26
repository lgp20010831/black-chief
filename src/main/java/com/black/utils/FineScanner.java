package com.black.utils;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.*;

public class FineScanner {

    static String classpath = FineScanner.class.getResource("/").getPath();

    public Set<Class<?>> scan(String packageName){
        return scan(packageName, Thread.currentThread().getContextClassLoader());
    }

    public Set<Class<?>> scan(String packageName, ClassLoader loader){
        Set<Class<?>> set = new HashSet<>();
        if (packageName == null){
            return set;
        }
        try {
            String path = packageName.replace(".","/");
            Enumeration<URL> enums = loader.getResources(path);
            String dirPath = "";
            while (enums.hasMoreElements()){
                URL url = enums.nextElement();
                if (url != null){
                    String protool = url.getProtocol();
                    if ("file".equals(protool)){
                        dirPath = url.getPath();
                        dirPath = URLDecoder.decode(dirPath,"utf-8");
                        scanForDir(dirPath, packageName, set);
                    }
                }
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
        return set;
    }

    private void scanForDir(String dirPath, String packageName, Set<Class<?>> set) throws ClassNotFoundException {
        //根据传入文件夹路径创建File对象
        File dir = new File(dirPath);
        doLoadFile(dir, set, packageName);
    }

    private void doLoadFile(File dir, Set<Class<?>> set, String packageName) throws ClassNotFoundException {
        if (dir.isDirectory()){
            for (File file : dir.listFiles()) {
                doLoadFile(file, set, packageName);
            }
        }else {
            if (dir.getName().endsWith(".class")){
                String path = dir.getPath();
                path = path.replace(classpath.replace("/","\\").replaceFirst("\\\\",""),"").replace("\\",".").replace(".class","");
                set.add(Class.forName(path));
            }
        }
    }



}

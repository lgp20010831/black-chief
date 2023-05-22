package com.black.core.cache;

import com.black.core.util.ClassUtils;
import com.black.scan.ScannerManager;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public final class ClassSourceCache {

    public static boolean printRegisterSize = false;
    private static final Map<String, Set<Class<?>>> sourceCache = new ConcurrentHashMap<>();

    public static void registerSource(String path, Set<Class<?>> source){
        if (sourceCache.containsKey(path)){
            return;
        }
        if (printRegisterSize && log.isInfoEnabled()) {
            log.info("path: {}, size:{}", path, source.size());
        }
        sourceCache.put(path, source);
    }

    public static Set<Class<?>> getSource(String path){
        if (sourceCache.containsKey(path)){
            return sourceCache.get(path);
        }

        int parentSize = 0;
        String parentPath = null;
        for (String cachePath : sourceCache.keySet()) {
            if (matchPath(cachePath, path)){
                if (parentPath == null){
                    parentPath = cachePath;
                    parentSize = sourceCache.get(cachePath).size();
                }else {
                    Set<Class<?>> set = sourceCache.get(cachePath);
                    if (set.size() < parentSize){
                        parentPath = cachePath;
                        parentSize = set.size();
                    }
                }
            }
        }
        if (parentPath == null){
            return null;
        }
        Set<Class<?>> parentSource = sourceCache.get(parentPath);
        Set<Class<?>> source = filterSource(parentSource, path);
        sourceCache.put(path, source);
        return source;
    }

    public static Set<Class<?>> filterSource(Set<Class<?>> sources, String path){
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> source : sources) {
            String targetPath = ClassUtils.getPackageName(source);
            if (matchPath(path, targetPath)){
                result.add(source);
            }
        }
        return result;
    }

    //比较两个地址是否为子集
    private static boolean matchPath(String src, String target){

        String[] srcParts = src.split("\\.");
        String[] targetParts = target.split("\\.");
        if (srcParts.length >  targetParts.length){
            return false;
        }
        for (int i = 0; i < srcParts.length; i++) {
            String srcPart = srcParts[i];
            String targetPart = targetParts[i];
            if (!srcPart.equals(targetPart)){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Set<Class<?>> set = ScannerManager.getScanner().load("com.black.xml");
        Set<Class<?>> set2 = ScannerManager.getScanner().load("com.black");
        registerSource("com.black.xml", set);
        registerSource("com.black", set2);
        System.out.println(getSource("com.black.xml.engine"));
    }

    public static void clear(){
        sourceCache.clear();
    }

    public static Map<String, Set<Class<?>>> getSourceCache() {
        return sourceCache;
    }
}

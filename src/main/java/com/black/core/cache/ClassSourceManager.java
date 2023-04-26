package com.black.core.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassSourceManager {

    //类扫描器
    private static ClassScanner scanner;

    public static void setScanner(ClassScanner scanner) {
        ClassSourceManager.scanner = scanner;
    }

    private static Map<String, ClassTree> tree = new ConcurrentHashMap<>();

    public static ClassScanner getScanner() {
        return scanner;
    }

    public static void init(String packageName){
        String[] sp = packageName.split("\\.");
        if (sp.length < 1)
            throw new RuntimeException(packageName);
        doScanAndTreelization(sp[0], packageName);
    }

    public static void clear(String first){
        tree.remove(first);
    }

    public static void clearAll(){
        tree.clear();
    }

    //传参格式 xxx.xxxx.xxxx
    public static Set<Class<?>> getClasses(String packageName){
        if (packageName == null) return new HashSet<>();

        //将包名全部拆分开
        String[] pns = packageName.split("\\.");
        String first = pns[0];
        ClassTree tree = ClassSourceManager.tree.computeIfAbsent(first, fn -> doScanAndTreelization(fn, packageName));
        return tree.getTargetClasses(packageName);
    }

    private static ClassTree doScanAndTreelization(String pn, String fpn){
        ClassScanner scanner = getScanner();
        if (scanner == null){
            throw new RuntimeException("scanner is null");
        }
        ClassTree head = new ClassTree(pn, null);
        Set<Class<?>> classes = scanner.scan(fpn);
        for (Class<?> c : classes) {
            head.parseClass(c);
        }
        return head;
    }
}

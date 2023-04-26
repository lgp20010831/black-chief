package com.black.core.cache;

import com.black.core.util.ClassUtils;
import com.black.core.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassTree {

    private final String packageName;

    private final ClassTree father;

    private final Map<String, ClassTree> sonNodes = new HashMap<>();

    private final Set<Class<?>> currentClasses = new HashSet<>();

    public ClassTree(String packageName, ClassTree father) {
        this.packageName = packageName;
        this.father = father;
    }

    public String getFullPackageName(){
        if (father == null) return getPackageName();
        return father.getFullPackageName() + "." + getPackageName();
    }

    public String getPackageName() {
        return packageName;
    }

    public Set<Class<?>> getTargetClasses(String pn){
        Set<Class<?>> result = new HashSet<>();
        if (!pn.startsWith(packageName)){
            return result;
        }

        String[] pns = pn.substring(packageName.length()).split("\\.");
        ClassTree currentNode = this;

        for (String p : pns) {
            if (!StringUtils.hasText(p))
                continue;
            if (currentNode.hasNode(p)){
                currentNode = currentNode.getNode(p);
            }else
                return result;
        }

        currentNode.push(result);
        return result;
    }

    public boolean hasNode(String name){
        return sonNodes.containsKey(name);
    }


    public ClassTree getNode(String name){
        return sonNodes.get(name);
    }

    public void push(Set<Class<?>> result){
        result.addAll(getCurrentClasses());
        sonNodes.forEach((name, node) ->{
            node.push(result);
        });
    }

    public Map<String, ClassTree> getSonNodes() {
        return sonNodes;
    }

    public Set<Class<?>> getCurrentClasses() {
        return currentClasses;
    }

    public void parseClass(Class<?> source){
        String fpn = getFullPackageName();
        int fpnl = fpn.length();
        String pn = ClassUtils.getPackageName(source);
        int i;
        if ((i = pn.indexOf(fpn)) == -1){
            throw new RuntimeException("error package name is [" + pn + "], should start with [" + fpn + "]");
        }

        String suffix = pn.substring(i + fpnl);
        if (StringUtils.hasText(suffix)){
            if (!suffix.startsWith(".")){
                throw new RuntimeException("inconsistent package name is [" + pn + "], should is [" + fpn + "]");
            }
            //将后面所有的包分开,
            // 第一个就是最近的子包
            String[] ps = suffix.substring(1).split("\\.");

            //子包名
            String spn = ps[0];
            ClassTree son = sonNodes.computeIfAbsent(spn, s -> new ClassTree(s, this));
            son.parseClass(source);
        }else {
            //没有后缀了, 则此 class 为当前包下存在的 class 文件
            currentClasses.add(source);
        }
    }
}

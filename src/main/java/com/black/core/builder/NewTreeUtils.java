package com.black.core.builder;

import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import java.util.*;


@Log4j2
public class NewTreeUtils {

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     Map<String, Object> filterConditionMap){
        return tree(source, (List<Sort>) null, filterConditionMap);
    }

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     Sort sortInfo,
                                                     Map<String, Object> filterConditionMap){
        return tree(source, "id", "pid", "children", Collections.singletonList(sortInfo), filterConditionMap, false);
    }

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     String cn,
                                                     Sort sortInfo,
                                                     Map<String, Object> filterConditionMap){
        return tree(source, "id", "pid", cn, Collections.singletonList(sortInfo), filterConditionMap, false);
    }

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     List<Sort> sortInfos,
                                                     Map<String, Object> filterConditionMap,
                                                     boolean like){
        return tree(source, "id", "pid", "children", sortInfos, filterConditionMap, like);
    }

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     List<Sort> sortInfos,
                                                     Map<String, Object> filterConditionMap){
        return tree(source, "id", "pid", "children", sortInfos, filterConditionMap, false);
    }

    public static <E> List<Map<String, Object>> tree(List<E> source, Sort sortInfos){
        return tree(source, "id", "pid", "children", Collections.singletonList(sortInfos));
    }

    public static <E> List<Map<String, Object>> tree(List<E> source, List<Sort> sortInfos){
        return tree(source, "id", "pid", "children", sortInfos);
    }


    public static <E> List<Map<String, Object>> tree(List<E> source){
        return tree(source, "id", "pid");
    }

    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     String id,
                                                     String pid){
        return tree(source, id, pid, "children", null);
    }


    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     String id,
                                                     String pid,
                                                     String childrenName){
        return tree(source, id, pid, childrenName, null);
    }


    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     String id,
                                                     String pid,
                                                     String childrenName,
                                                     List<Sort> sortInfos){
        return tree(source, id, pid, childrenName, sortInfos, null, false);
    }

    /***
     * 将一个 list 转成树结构 2n
     * @param source 要转成树的集合
     * @param id 唯一标识
     * @param pid 父类标识
     * @param childrenName 子类集合在父类结果集中存在的key
     * @param sortInfos 要排序的信息
     * @param filterConditionMap 过滤条件 map
     * @param <E> 泛型
     * @return 返回转树后的结果
     */
    public static <E> List<Map<String, Object>> tree(List<E> source,
                                                     String id,
                                                     String pid,
                                                     String childrenName,
                                                     List<Sort> sortInfos,
                                                     Map<String, Object> filterConditionMap,
                                                     boolean like){

        final Map<String, Map<String, Object>> mappings = new HashMap<>();
        List<Map<String, Object>> resultMappings = new ArrayList<>();

        for (E e : source) {
            Map<String, Object> map = ServiceUtils.buildMapping(e);
            map.computeIfAbsent(childrenName, c -> new ArrayList<>());
            mappings.put(ServiceUtils.getString(map, id), map);
        }
        String tkey;
        for (String mid : mappings.keySet()) {
            Map<String, Object> map = mappings.get(mid);
            if (mappings.containsKey(tkey = ServiceUtils.getString(map, pid))){
                if(tkey.equals(id)){
                    if (log.isWarnEnabled()) {
                        log.warn("父节点和自身节点标识符不能相同,会造成死循环");
                    }
                    resultMappings.add(map);
                    continue;
                }

                Map<String, Object> superMapping = mappings.get(tkey);
                List<Map<String, Object>> childrens = (List<Map<String, Object>>) superMapping.computeIfAbsent(childrenName, c -> new ArrayList<>());
                childrens.add(map);
            }else {
                resultMappings.add(map);
            }
        }

        if (!Utils.isEmpty(filterConditionMap)){
            List<Map<String, Object>> afterFilterResultMapping = new ArrayList<>();
            for (Map<String, Object> resultMapping : resultMappings) {
                loopFilter(afterFilterResultMapping, resultMapping, filterConditionMap, childrenName, like);
            }
            resultMappings = afterFilterResultMapping;
        }

        if (!Utils.isEmpty(sortInfos)){
            loopsort(resultMappings, childrenName, sortInfos);
        }

        return resultMappings;
    }

    static void loopFilter(List<Map<String, Object>> result,
                           Map<String, Object> source,
                           Map<String, Object> condition,
                           String childrenName,
                           boolean like){
        if (filter(source, condition, like)) {
            result.add(source);
            return;
        }
        List<Map<String, Object>> childrens = (List<Map<String, Object>>) source.get(childrenName);
        for (Map<String, Object> children : childrens) {
            loopFilter(result, children, condition, childrenName, like);
        }
    }

    static boolean filter(Map<String, Object> source, Map<String, Object> condition, boolean like){
        boolean save = true;
        for (String key : condition.keySet()) {
            if (source.containsKey(key)){
                Object o = source.get(key);
                Object c = condition.get(key);
                if (like){
                    if (o != null && c != null){
                        if (!o.toString().contains(c.toString())) {
                            save = false;
                            break;
                        }
                    }
                }else {
                    if (!Objects.equals(o, c)) {
                        save = false;
                        break;
                    }
                }
            }
        }
        return save;
    }

    static void loopsort(List<Map<String, Object>> source, String childrenName, List<Sort> sortInfos){
        ServiceUtils.sort(source, sortInfos);
        for (Map<String, Object> map : source) {
            loopsort((List<Map<String, Object>>) map.get(childrenName), childrenName, sortInfos);
        }
    }

}

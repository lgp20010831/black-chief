package com.black.core.builder;

import com.black.core.util.Utils;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class TreeBuilder {


    public static <E> TreeConfig<E> prepare(List<E> source){
        if (source == null){
            source = new ArrayList<>();
        }
        return new TreeConfig<>(source);
    }

    public static class TreeConfig<E>{
        final List<E> source;
        String id = "id";
        String pid = "pid";
        String childrenName = "children";
        List<Sort> sortInfos = new ArrayList<>();
        Map<String, Object> filterConditionMap = new HashMap<>();
        boolean like = false;

        public TreeConfig(List<E> source) {
            this.source = source;
        }

        public TreeConfig<E> id(String id){
            this.id = id;
            return this;
        }

        public TreeConfig<E> pid(String pid){
            this.pid = pid;
            return this;
        }

        public TreeConfig<E> child(String child){
            this.childrenName = child;
            return this;
        }

        public TreeConfig<E> sorts(Sort... sorts){
            if (sorts != null){
                this.sortInfos.addAll(Arrays.asList(sorts));
            }
            return this;
        }

        public TreeConfig<E> sorts(List<Sort> sorts){
            if (sorts != null){
                this.sortInfos.addAll(sorts);
            }
            return this;
        }

        public TreeConfig<E> condition(Map<String, Object> c){
            if (c != null){
                this.filterConditionMap.putAll(c);
            }

            return this;
        }

        public TreeConfig<E> like(boolean like){
            this.like = like;
            return this;
        }

        public List<Map<String, Object>> exceute(){
            return NewTreeUtils.tree(source, id, pid, childrenName, sortInfos, filterConditionMap, like);
        }

        public List<TreeEntity> exceuteAndGetEntity(){
            return tree(source, id, pid, childrenName, sortInfos, filterConditionMap, like, this);
        }
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
    public static <E> List<TreeEntity> tree(List<E> source,
                                                     String id,
                                                     String pid,
                                                     String childrenName,
                                                     List<Sort> sortInfos,
                                                     Map<String, Object> filterConditionMap,
                                                     boolean like,
                                                     TreeConfig<E> config){

        final Map<String, Map<String, Object>> mappings = new HashMap<>();
        List<TreeEntity> resultMappings = new ArrayList<>();

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
                    resultMappings.add(new TreeEntity(config, map));
                    continue;
                }

                Map<String, Object> superMapping = mappings.get(tkey);
                List<TreeEntity> childrens = (List<TreeEntity>) superMapping.computeIfAbsent(childrenName, c -> new ArrayList<>());
                childrens.add(new TreeEntity(config, map));
            }else {
                resultMappings.add(new TreeEntity(config, map));
            }
        }

        if (!Utils.isEmpty(filterConditionMap)){
            List<TreeEntity> afterFilterResultMapping = new ArrayList<>();
            for (TreeEntity resultMapping : resultMappings) {
                loopFilter(afterFilterResultMapping, resultMapping, filterConditionMap, childrenName, like);
            }
            resultMappings = afterFilterResultMapping;
        }

        if (!Utils.isEmpty(sortInfos)){
            for (Sort sortInfo : sortInfos) {
                String sortName = sortInfo.getSortName();
                sortInfo.setSortName("data." + sortName);
            }
            loopsort(resultMappings, childrenName, sortInfos);
        }

        return resultMappings;
    }

    static void loopFilter(List<TreeEntity> result,
                           TreeEntity entity,
                           Map<String, Object> condition,
                           String childrenName,
                           boolean like){
        Map<String, Object> source = entity.getData();
        if (filter(source, condition, like)) {
            result.add(entity);
            return;
        }
        List<TreeEntity> childrens = (List<TreeEntity>) source.get(childrenName);
        for (TreeEntity children : childrens) {
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

    static void loopsort(List<TreeEntity> entities, String childrenName, List<Sort> sortInfos){
        ServiceUtils.sort(entities, sortInfos);
        for (TreeEntity entity : entities) {
            loopsort(entity.getChild(), childrenName, sortInfos);
        }
    }


}

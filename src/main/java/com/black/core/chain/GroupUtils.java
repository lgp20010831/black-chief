package com.black.core.chain;

import com.alibaba.fastjson.JSONObject;
import com.black.core.builder.Col;
import com.black.core.spring.util.ApplicationUtil;

import java.util.*;
import java.util.stream.Collectors;

public class GroupUtils {


    public static <G, E> Map<G, Group<E>> group(Collection<E> source, GroupBy<G, E> by){
        Map<G, Group<E>> result = new LinkedHashMap<>();
        Set<G> middleware = new HashSet<>();
        for (E o : source) {
            G condition;
            if (middleware.contains(condition = by.groupBy(o))){
                result.get(condition).getSource().add(o);
            }else {
                Group<E> group = result.computeIfAbsent(condition, k -> new Group<>());
                group.getSource().add(o);
            }
        }
        return result;
    }

    public static <G, E> Map<G, List<E>> groupArray(Collection<E> source, GroupBy<G, E> by){
        Map<G, List<E>> result = new LinkedHashMap<>();
        for (E o : source) {
            G condition = by.groupBy(o);
            List<E> group = result.computeIfAbsent(condition, k -> new ArrayList<>());
            group.add(o);
        }
        return result;
    }

    public static <G, E> Map<G, E> singleGroupArray(Collection<E> source, GroupBy<G, E> by){
        Map<G, E> result = new LinkedHashMap<>();
        for (E e : source) {
            G group = by.groupBy(e);
            result.put(group, e);
        }
        return result;
    }

    public static <G, E> Map<G, List<E>> groupList(Collection<E> source, GroupBy<G, E> by){
        Map<G, List<E>> result = new LinkedHashMap<>();
        Set<G> middleware = new HashSet<>();
        for (E o : source) {
            G condition;
            if (middleware.contains(condition = by.groupBy(o))){
                result.get(condition).add(o);
            }else {
                List<E> group = result.computeIfAbsent(condition, k -> new ArrayList<>());
                group.add(o);
            }
        }
        return result;
    }

    public static <G, E> Map<G, List<E>> stearmGroup(Collection<E> source, GroupBy<G, E> by){
        return source.stream()
                .collect(Collectors.toMap(by::groupBy, element -> {
                    List<E> set = new ArrayList<>();
                    set.add(element);
                    return set;
                }, (o, n) -> {
                    o.addAll(n);
                    return o;
                }, HashMap::new));
    }

    public static void main(String[] args) {
        List<JSONObject> as = new ArrayList<>();
        for (int i = 0; i < 2000000; i++) {
            JSONObject js = Col.js("id", i, "name", "lgp");
            as.add(js);
        }
        ApplicationUtil.programRunMills(() ->{
            System.out.println(groupList(as, e -> e.getString("id")).size());
        });
        ApplicationUtil.programRunMills(() ->{
            System.out.println(stearmGroup(as, e -> e.getString("id")).size());
        });
        ApplicationUtil.programRunMills(() ->{
            System.out.println(groupList(as, e -> e.getString("id")).size());
        });
        ApplicationUtil.programRunMills(() ->{
            System.out.println(group(as, e -> e.getString("id")).size());
        });
        ApplicationUtil.programRunMills(() ->{
            System.out.println(groupArray(as, e -> e.getString("id")).size());
        });
    }
}

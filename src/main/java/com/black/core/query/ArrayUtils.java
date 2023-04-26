package com.black.core.query;

import com.black.core.util.Av0;
import lombok.NonNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ArrayUtils {

    @SafeVarargs
    public static <E> Array<E> toArray(E... array){
        return new AscArray<>(array);
    }

    @SafeVarargs
    public static <E> Array<E> toArrayDesc(E... array){
        return new DescArray<>(array);
    }

    public interface ArraysFor<S, D>{
        void loop(S s, D d);
    }

    public interface ArraysMoreFor<S, D, F>{
        void loop(S s, D d, F f);
    }


    public interface MapStateFor<K, V>{
        void loop(K k, V v, boolean last, boolean first);
    }

    public interface ArrayStateFor<S>{
        void loop(S s, boolean last, boolean first);
    }

    public interface ArrayStateIndexFor<S>{
        void loop(S s, boolean last, boolean first, int index);
    }

    public static <E> List<E> foreach(List<E> list, Consumer<? super E> consumer){
        if (!isEmpty(list)){
            int a = list.size();
            for (int i = 0; i < a; i++) {
                E e = list.get(i);
                consumer.accept(e);
            }
        }
        return list;
    }

    public static <T> boolean isEmpty(Collection<T> collection){
        return collection == null || collection.isEmpty();
    }

    public static <K, V> void stateFor(@NonNull Map<K, V> map, @NonNull MapStateFor<K, V> stateFor){
        int i = map.size();
        for (K k : map.keySet()) {
            V v = map.get(k);
            boolean first = i-- == map.size();
            boolean last = i == 0;
            stateFor.loop(k, v, first, last);
        }
    }

    public static <S> void stateFor(@NonNull List<S> array, @NonNull ArrayStateFor<S> stateFor){
        for (int i = 0; i < array.size(); i++) {
            boolean first = i == 0;
            boolean last = i == array.size() - 1;
            stateFor.loop(array.get(i), last, first);
        }
    }

    public static <S> void stateFor(@NonNull List<S> array, @NonNull ArrayStateIndexFor<S> stateFor){
        for (int i = 0; i < array.size(); i++) {
            boolean first = i == 0;
            boolean last = i == array.size() - 1;
            stateFor.loop(array.get(i), last, first, i);
        }
    }

    public static <S> void stateFor(@NonNull S[] array, @NonNull ArrayStateFor<S> stateFor){
        for (int i = 0; i < array.length; i++) {
            boolean first = i == 0;
            boolean last = i == array.length - 1;
            stateFor.loop(array[i], last, first);
        }
    }

    public static <S> void stateFor(@NonNull S[] array, @NonNull ArrayStateIndexFor<S> stateFor){
        for (int i = 0; i < array.length; i++) {
            boolean first = i == 0;
            boolean last = i == array.length - 1;
            stateFor.loop(array[i], last, first, i);
        }
    }

    public static <K, V> void sortMapStateFor(@NonNull Map<K, V> map,
                                         @NonNull Function<V, Integer> function,
                                              @NonNull MapStateFor<K, V> stateFor){
        Map<Integer, K> sortlist = new HashMap<>();
        for (K key : map.keySet()) {
            V v = map.get(key);
            sortlist.put(function.apply(v), key);
        }
        ArrayList<Integer> list = new ArrayList<>(sortlist.keySet());
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            K k = sortlist.get(list.get(i));
            V v = map.get(k);
            boolean first = i == 0;
            boolean last = i == list.size() - 1;
            stateFor.loop(k, v, first, last);
        }
    }

    public static <K, V> void sortMapFor(@NonNull Map<K, V> map,
                                         @NonNull Function<V, Integer> function,
                                         @NonNull BiConsumer<K, V> consumer){
        Map<Integer, K> sortlist = new HashMap<>();
        for (K key : map.keySet()) {
            V v = map.get(key);
            sortlist.put(function.apply(v), key);
        }
        ArrayList<Integer> list = new ArrayList<>(sortlist.keySet());
        Collections.sort(list);
        for (Integer i : list) {
            K k = sortlist.get(i);
            V v = map.get(k);
            consumer.accept(k, v);
        }
    }

    public static void main(String[] args) {
        List<Integer> list = Av0.as(1, 9, 8, 3, 4, 7);
        list.sort(null);
        System.out.println(list);
    }


    public static <S, D> Collection<S> loops(@NonNull Collection<S> src, @NonNull Collection<D> dest, @NonNull ArraysFor<S, D> arraysFor){
        int fori = Math.min(src.size(), dest.size());
        List<S> ls = src instanceof List ? (List<S>) src : new ArrayList<>(src);
        List<D> ds = dest instanceof List ? (List<D>) dest : new ArrayList<>(dest);
        for (int i = 0; i < fori; i++) {
            arraysFor.loop(ls.get(i), ds.get(i));
        }
        return src;
    }

    public static <S, D, F> Collection<S> loops(@NonNull Collection<S> src,
                                             @NonNull Collection<D> dest,
                                             @NonNull Collection<F> three,
                                             @NonNull ArraysMoreFor<S, D, F> arraysFor){
        int fori = Math.min(src.size(), dest.size());
        fori = Math.min(fori, three.size());
        List<S> ls = src instanceof List ? (List<S>) src : new ArrayList<>(src);
        List<D> ds = dest instanceof List ? (List<D>) dest : new ArrayList<>(dest);
        List<F> ts = three instanceof List ? (List<F>) three : new ArrayList<>(three);
        for (int i = 0; i < fori; i++) {
            arraysFor.loop(ls.get(i), ds.get(i), ts.get(i));
        }
        return src;
    }


    public static <S, D> Array<S> loops(@NonNull Array<S> src, @NonNull Array<D> dest, @NonNull ArraysFor<S, D> arraysFor){
        int fori = Math.min(src.length(), dest.length());
        for (int i = 0; i < fori; i++) {
            arraysFor.loop(src.get(i), dest.get(i));
        }
        return src;
    }

    public static <S, D> S[] loops(@NonNull S[] src, @NonNull D[] dest, @NonNull ArraysFor<S, D> arraysFor){
        int fori = Math.min(src.length, dest.length);
        for (int i = 0; i < fori; i++) {
            arraysFor.loop(src[i], dest[i]);
        }
        return src;
    }

    public static <S, D> boolean equalsArray(@NonNull S[] src, @NonNull D[] dest){
        if (src.length != dest.length){
            return false;
        }
        for (int i = 0; i < src.length; i++) {
            S s = src[i];
            D d = dest[i];
            if (s == null && d == null){
                continue;
            }
            if (s != null && !s.equals(d)){
                return false;
            }else if (d != null && !d.equals(s)){
                return false;
            }
        }
        return true;
    }

    public static <E> List<E> loopBatch(List<E> list, Consumer<List<E>> consumer, int batch){
        if (isEmpty(list)){
            return list;
        }
        ArrayList<E> copy = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int b = i + 1;
            E ele = list.get(i);
            copy.add(ele);
            if (b % batch == 0){
                consumer.accept(copy);
                copy.clear();
            }
        }

        if (!copy.isEmpty()){
            consumer.accept(copy);
        }
        return list;
    }

    public static <E, R> List<R> loopSumBatch(List<E> list, Function<List<E>, List<R>> function, int batch){
        if (isEmpty(list)){
            return new ArrayList<>();
        }
        ArrayList<R> result = new ArrayList<>();
        ArrayList<E> copy = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int b = i + 1;
            E ele = list.get(i);
            copy.add(ele);
            if (b % batch == 0){
                List<R> apply = function.apply(copy);
                if (apply != null){
                    result.addAll(apply);
                }
                copy.clear();
            }
        }

        if (!copy.isEmpty()){
            List<R> apply = function.apply(copy);
            if (apply != null){
                result.addAll(apply);
            }
        }
        return result;
    }

}

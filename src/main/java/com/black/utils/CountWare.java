package com.black.utils;

import lombok.NonNull;

import java.util.*;
import java.util.function.Function;

/**
 *  用来处理复杂序号的类
 *  CountWare cw = new CountWare("1.0");
 *  cw.addLast(); // cw = 1.1
 *  cw.set(index:2, val:5) //cw = 1.5
 *  cw.reduceLast() // cw = 1.4
 *  cw.addChild(0) //cw = 1.4.0
 *  cw.removeFirst() //cw = 4.0
 *  同样可以解决复杂序号排序的问题
 *  List<String> list = Arrays.asList("1.2", "1.1", "1.2", "1.100.1", "1.40", "1.3", "1.15");
 *  CountWare.sortCountWare(list, CountWare::new, true);
 *  // list = [1, 1.1, 1.2, 1.2, 1.3, 1.15, 1.40, 1.100.1]
 */
public final class CountWare implements Comparator<CountWare>{

    private Integer[] array;

    public CountWare(){
        this(0, 1);
    }

    public CountWare(int size){
        this(0, size);
    }

    public CountWare(int initVal, int size){
        this(create(initVal, size));
    }

    public CountWare(@NonNull String txt){
        String[] eles = txt.split("\\.");
        array = new Integer[eles.length];
        for (int i = 0; i < eles.length; i++) {
            String ele = eles[i];
            int val = Integer.parseInt(ele);
            array[i] = val;
        }
    }

    @Override
    public int compare(CountWare c1, CountWare c2) {
        int min = Math.min(c1.size(), c2.size());
        for (int i = 0; i < min; i++) {
            int i1 = c1.get(i);
            int i2 = c2.get(i);
            if (i1 == i2)
                continue;
            return i1 - i2;
        }
        return c1.size() - c2.size();
    }

    public static <T> void sortCountWare(List<T> list, Function<T, CountWare> function, boolean asc){
        list.sort((o1, o2) -> {
            CountWare c1 = function.apply(o1);
            CountWare c2= function.apply(o2);
            return asc ? c1.compare(c1, c2) : c1.compare(c2, c1);
        });

    }

    public static String create(int initVal, int size){
        StringJoiner joiner = new StringJoiner(".");
        for (int i = 0; i < size; i++) {
            joiner.add(String.valueOf(initVal));
        }
        return joiner.toString();
    }

    public int get(int index){
        return array[index];
    }

    private void check(int index){
        if (index > array.length){
            throw new IndexOutOfBoundsException("array size is " + array.length + ", but point index is " + index);
        }
    }

    private void doCapacity(int range){
        Integer[] newBuffer = new Integer[array.length + range];
        System.arraycopy(array, 0, newBuffer, 0, array.length);
        array = newBuffer;
    }

    private void doDecrement(int range, int start){
        Integer[] newBuffer = new Integer[range];
        System.arraycopy(array, start, newBuffer, 0, range);
        array = newBuffer;
    }

    public int size(){
        return array.length;
    }

    public void set(int index, int val){
        check(index);
        array[index - 1] = val;
    }

    public void addChild(int initVal){
        doCapacity(1);
        set(size(), initVal);
    }

    public void removeLast(){
        doDecrement(size() - 1, 0);
    }

    public void removeFirst(){
        doDecrement(size() - 1, 1);
    }

    public void add(int index){
        add(index, 1);
    }

    public void add(int index, int val){
        check(index);
        set(index, current(index) + val);
    }

    public void addFirst(){
        addFirst(1);
    }

    public void addFirst(int val){
        add(1, val);
    }

    public void addLast(){
        addLast(1);
    }

    public void addLast(int val){
        add(size(), val);
    }

    public void reduce(int index){
        reduce(index, 1);
    }

    public void reduce(int index, int val){
        int current = current(index);
        val = current > val ? current - val : 0;
        set(index, val);
    }

    public void reduceFirst(){
        reduceFirst(1);
    }

    public void reduceFirst(int val){
        reduce(1, val);
    }

    public void reduceLast(){
        reduceLast(1);
    }

    public void reduceLast(int val){
        reduce(size(), val);
    }

    private int current(int index){
        return array[index - 1];
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(".");
        for (Integer ele : array) {
            joiner.add(ele.toString());
        }
        return joiner.toString();
    }

}

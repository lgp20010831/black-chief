package com.black.ftl;

import lombok.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
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
public final class SerialNum implements Comparator<SerialNum>{

    private Integer[] array;

    public SerialNum(){
        this(0, 1);
    }

    public SerialNum(int size){
        this(0, size);
    }

    public SerialNum(int initVal, int size){
        this(create(initVal, size));
    }

    public SerialNum(@NonNull String txt){
        String[] eles = txt.split("\\.");
        array = new Integer[eles.length];
        for (int i = 0; i < eles.length; i++) {
            String ele = eles[i];
            int val = Integer.parseInt(ele);
            array[i] = val;
        }
    }

    @Override
    public int compare(SerialNum c1, SerialNum c2) {
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

    public static <T> void sortCountWare(List<T> list, Function<T, SerialNum> function, boolean asc){
        list.sort((o1, o2) -> {
            SerialNum c1 = function.apply(o1);
            SerialNum c2= function.apply(o2);
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

    public SerialNum removeLast(){
        doDecrement(size() - 1, 0);
        return this;
    }

    public SerialNum removeFirst(){
        doDecrement(size() - 1, 1);
        return this;
    }

    public SerialNum add(int index){
        add(index, 1);
        return this;
    }

    public SerialNum add(int index, int val){
        check(index);
        set(index, current(index) + val);
        return this;
    }

    public SerialNum addFirst(){
        addFirst(1);
        return this;
    }

    public SerialNum addFirst(int val){
        add(1, val);
        return this;
    }

    public SerialNum addLast(){
        addLast(1);
        return this;
    }

    public SerialNum addLast(int val){
        add(size(), val);
        return this;
    }

    public SerialNum reduce(int index){
        reduce(index, 1);
        return this;
    }

    public SerialNum reduce(int index, int val){
        int current = current(index);
        val = current > val ? current - val : 0;
        set(index, val);
        return this;
    }

    public SerialNum reduceFirst(){
        reduceFirst(1);
        return this;
    }

    public SerialNum reduceFirst(int val){
        reduce(1, val);
        return this;
    }

    public SerialNum reduceLast(){
        reduceLast(1);
        return this;
    }

    public SerialNum reduceLast(int val){
        reduce(size(), val);
        return this;
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

package com.black.core.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayBuilder {

    public static ArrayGovern<Object> builder(){
        return new ArrayGovern<>();
    }

    public static <V> ArrayGovern<V> machining(List<V> source){
        return new ArrayGovern<V>(source);
    }

    public static class ArrayGovern<V>{

        final List<V> list;

        public ArrayGovern(){
            this(new ArrayList<V>());
        }

        public ArrayGovern(List<V> list) {
            this.list = list;
        }

        public ArrayGovern<V> add(V value){
            list.add(value);
            return this;
        }

        public ArrayGovern<V> addAll(Collection<V> collection){
            list.addAll(collection);
            return this;
        }

        public List<V> build(){
            return list;
        }
    }
}

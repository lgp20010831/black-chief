package com.black.fun_net;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class NetDemo {

    public static void main(String[] args) {
        ClassMap<String> map = new ClassMap<>();
        map.put(Map.class, "str");
        System.out.println(map.get(HashMap.class));
    }
}

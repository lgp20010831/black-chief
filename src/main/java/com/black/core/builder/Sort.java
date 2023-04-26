package com.black.core.builder;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter @AllArgsConstructor
public class Sort {

    String sortName;
    boolean asc;

    public Sort(String sortName) {
        this.sortName = sortName;
        asc = true;
    }

    public static List<Sort> sorts(String... name){
        return sorts(true, name);
    }

    public static List<Sort> sorts(boolean asc, String... name){
        List<Sort> sorts = new ArrayList<>();
        for (String n : name) {
            sorts.add(new Sort(n, asc));
        }
        return sorts;
    }

    public static Sort sort(String name){
        return sort(name, true);
    }

    public static Sort sort(String name, boolean asc){
        return new Sort(name, asc);
    }
}

package com.black.core.util;

import com.black.core.convert.ConversionWay;
import com.black.core.convert.TypeContributor;

import java.util.List;
import java.util.Set;

@TypeContributor
public class MappingTypeConvert {


    @ConversionWay
    <T> List<T> toList(T[] array){
        return Av0.as(array);
    }

    @ConversionWay
    <T>Set<T> toSet(T[] array){
        return Av0.set(array);
    }

    @ConversionWay
    Set<String> toSet(String[] array){
        return Av0.set(array);
    }

    @ConversionWay
    List<String> toList(String[] array){
        return Av0.as(array);
    }

    @ConversionWay
    String[] toArray(Set<String> set){return set.toArray(new String[0]);}

    @ConversionWay
    String[] toArray(List<String> set){return set.toArray(new String[0]);}

    @ConversionWay
    int toi(String s){return Integer.parseInt(s);}

    @ConversionWay
    boolean cb(Boolean b){
        return b != null && b;
    }
}

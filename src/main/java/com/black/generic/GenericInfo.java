package com.black.generic;

import com.black.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-09 11:02
 */
@SuppressWarnings("all")
public class GenericInfo {

    private final List<Generic> generics = new ArrayList<>();

    public void addGeneric(Generic generic){
        generics.add(generic);
    }

    public int size(){
        return generics.size();
    }

    public boolean isEmpty(){
        return generics.isEmpty();
    }

    public Class<?> getClass(int index){
        return generics.get(index).getGenericType();
    }

    public Generic getGeneric(int index){
        return generics.get(index);
    }

    public List<Generic> getGenerics() {
        return generics;
    }

    public Class<?> getDeepGeneric(int index){
        Generic generic = getGeneric(index);
        return generic.getDeepGeneric();
    }

    public static GenericInfo group(Generic... generics){
        GenericInfo info = new GenericInfo();
        for (Generic generic : generics) {
            info.addGeneric(generic);
        }
        return info;
    }

    @Override
    public String toString() {
        return StringUtils.joinStringWithComplete("",
                "<", ">;", generics.toArray());
    }
}

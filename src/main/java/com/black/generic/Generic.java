package com.black.generic;

import com.black.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-09 11:04
 */
@SuppressWarnings("all")
public class Generic {

    private final List<Generic> sons = new ArrayList<>();

    private Class<?> genericType;

    public static Generic of(Class<?> type, Generic... sons){
        Generic generic = new Generic(type);
        for (Generic son : sons) {
            generic.addGeneric(son);
        }
        return generic;
    }

    public Generic(Class<?> genericType) {
        this.genericType = genericType;
    }

    public Generic() {

    }

    public void setGenericType(Class<?> genericType) {
        this.genericType = genericType;
    }

    public boolean hasMore(){
        return !sons.isEmpty();
    }

    public void addGeneric(Generic generic){
        sons.add(generic);
    }

    public Generic get(int index){
        return sons.get(index);
    }

    public Class<?> getGenericType() {
        return genericType;
    }

    public Class<?> getDeepGeneric(){
        if (sons.isEmpty()){
            return getGenericType();
        }else {
            return sons.get(0).getDeepGeneric();
        }
    }

    public Generic getGeneric(int index){
        return sons.get(index);
    }

    public List<Generic> getGenerics(){
        return sons;
    }

    @Override
    public String toString() {
        return "L" + genericType.getCanonicalName().replace(".", "/") + (hasMore() ?
                StringUtils.joinStringWithComplete(",",
                        "<", ">;", sons.toArray()) : ";");
    }
}

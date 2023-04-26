package com.black.blent;

import java.util.List;
import java.util.StringJoiner;

public interface Blent {

    //获取平面值
    List<String> getPlanes();

    //否则是 json array
    boolean isJson();

    //获取子 blent 数据
    List<Blent> getBlentChilds();

    //获取别名
    String getAlias();

    //添加子 blent 数据
    void addBlent(Blent blent);

    default String getBlentDesc(){
        List<String> planes = getPlanes();
        StringJoiner joiner = new StringJoiner("_");
        for (String plane : planes) {
            joiner.add(plane);
        }
        return joiner.toString();
    }
}

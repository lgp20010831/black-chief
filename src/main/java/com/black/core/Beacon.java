package com.black.core;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.util.LazyAutoWried;
import lombok.Getter;
import org.springframework.util.ClassUtils;

//坐标
@Getter @SuppressWarnings("all")
public final class Beacon {

    @LazyAutoWried
    protected String[] arr;

    public Beacon(){
        arr = new String[]{"b", "e", "a", "c", "o", "n"};
    }

    public static String getPackageName(){
        return ClassUtils.getPackageName(Beacon.class);
    }


    public static void main(String[] args) {
        Beacon beacon = new Beacon();
        JSONObject json = JsonUtils.letV2Json(beacon);
        System.out.println(json);
    }

}




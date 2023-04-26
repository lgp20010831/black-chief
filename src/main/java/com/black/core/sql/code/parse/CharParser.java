package com.black.core.sql.code.parse;

import com.black.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharParser {

    public static Map<String, BlendObjects> toMaps(List<BlendObjects> blendObjects){
        Map<String, BlendObjects> map = new HashMap<>();
        for (BlendObjects blendObject : blendObjects) {
            map.put(blendObject.name, blendObject);
        }
        return map;
    }


    public static Map<String, BlendObject> toMap(List<BlendObject> blendObjects){
        Map<String, BlendObject> map = new HashMap<>();
        for (BlendObject blendObject : blendObjects) {
            map.put(blendObject.name, blendObject);
        }
        return map;
    }

    public static List<BlendObjects> parseBlends(String context){
        if (!StringUtils.hasText(context)){
            return new ArrayList<>();
        }
        List<BlendObjects> attributtes = new ArrayList<>();
        StringBuilder name = new StringBuilder();
        BlendObjects[] fathers = new BlendObjects[25];
        int step = 0;
        for (char chr : context.toCharArray()) {

            if (chr == ' ') continue;

            if (chr == ',' ){
                if (name.length() > 0){
                    if (step > 0){
                        fathers[step - 1].add(name.toString());
                    }
                    name.delete(0, name.length());
                }
            }
            else
            if (chr == '['){
                BlendObjects ra = new BlendObjects(name.toString());
                fathers[step ++] = ra;
                if (step != 1){
                    fathers[step - 2].add(ra);
                }
                name.delete(0, name.length());
            }
            else
            if (chr == ']'){
                if (name.length() > 0){
                    fathers[step - 1].add(name.toString());
                }
                if (step - 1 == 0){
                    attributtes.add(fathers[step - 1]);
                }
                fathers[-- step] = null;
                name.delete(0, name.length());
            }
            else {
                name.append(chr);
            }
        }

        return attributtes;
    }

    public static List<BlendObject> parseBlend(String txt){
        if (!StringUtils.hasText(txt)){
            return new ArrayList<>();
        }
        List<BlendObject> attributtes = new ArrayList<>();
        StringBuilder name = new StringBuilder();
        BlendObject currentBlend = null;
        char[] charArray = txt.toCharArray();
        for (char chr : charArray) {
            if (chr == ' ') continue;
            if (chr == ',' ){
                if (name.length() > 0 && currentBlend != null){
                    currentBlend.add(name.toString());
                }
                name.delete(0, name.length());
            }
            else
            if (chr == '['){
                if (currentBlend == null){
                    currentBlend = new BlendObject(name.toString());
                }else {
                    throw new IllegalStateException("ill txt: " + txt);
                }
                name.delete(0, name.length());
            }
            else
            if (chr == ']'){
                if (currentBlend != null){
                    if (name.length() > 0){
                        currentBlend.add(name.toString());
                        attributtes.add(currentBlend);
                        currentBlend = null;
                    }
                }else {
                    throw new IllegalStateException("ill txt: " + txt);
                }

                name.delete(0, name.length());
            }
            else {
                name.append(chr);
            }
        }
        return attributtes;
    }

    public static void main(String[] args) {
//        List<BlendObject> blendObjects = parseBlend(",like[name, code], >[age]], <>[email]");
        List<BlendObjects> list = parseBlends("add[supplier[code, id], driver[driver]], update[supplier[id], user[name]]");
        System.out.println(list);
        System.out.println(toMaps(list));

    }

}

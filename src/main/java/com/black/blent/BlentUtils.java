package com.black.blent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.function.Function;
import com.black.core.util.StringUtils;
import lombok.NonNull;

import java.util.List;

public class BlentUtils {

    enum STATE{
        FIRST,
        LOOP,
        BUILD
    }
    
    public static Object resolveBlent(@NonNull Blent blent, @NonNull Function<String, JSONObject> function){
        List<String> planes = blent.getPlanes();
        JSONObject json = new JSONObject();
        for (String plane : planes) {
            try {
                JSONObject planJson = function.apply(plane);
                json.putAll(planJson);
            } catch (Throwable e) {
                throw new IllegalStateException("execute plane to json fair:" + plane, e);
            }
        }
        List<Blent> blentChilds = blent.getBlentChilds();
        for (Blent child : blentChilds) {
            loop(json, child, function);
        }
        if (!blent.isJson()){
            JSONArray array = new JSONArray();
            array.add(json);
            return array;
        }
        return json;
    }

    private static void loop(JSONObject json, Blent blent, Function<String, JSONObject> function){
        List<String> planes = blent.getPlanes();
        if (planes.size() == 1){
            String p = planes.get(0);
            JSONObject sonJson = new JSONObject();
            if (blent.isJson()) {
                json.put(blent.getAlias(), sonJson);
                try {
                    JSONObject applyJson = function.apply(p);
                    sonJson.putAll(applyJson);
                } catch (Throwable e) {
                    throw new IllegalStateException("execute plane to json fair:" + p, e);
                }
            }else {
                JSONArray array = new JSONArray();
                json.put(blent.getAlias(), array);
                try {
                    JSONObject applyJson = function.apply(p);
                    sonJson.putAll(applyJson);
                } catch (Throwable e) {
                    throw new IllegalStateException("execute plane to json fair:" + p, e);
                }
                array.add(sonJson);
            }
            for (Blent object : blent.getBlentChilds()) {
                loop(sonJson, object, function);
            }
        }else {
            throw new IllegalStateException("loop plane must = 1");
        }
    }   

    //supplier, driver[supplierAddress(address){person(person){}}, supplierLicence(licenceList)[]]
    public static Blent parseBlends(String context){
        if (!StringUtils.hasText(context)){
            return null;
        }
        DefaultBlent blent = new DefaultBlent();
        StringBuilder name = new StringBuilder();
        DefaultBlent[] fathers = new DefaultBlent[16];
        fathers[0] = blent;
        STATE state = STATE.FIRST;
        int step = 0;
        for (char chr : context.toCharArray()) {
            if (chr == ' ') continue;
            if (chr == '\n') continue;
            if (chr == ',' ){
                if (name.length() > 0){
                    switch (state){
                        case FIRST:
                            fathers[step].planes.add(name.toString());
                            break;
                        default:
                            throw new IllegalStateException("异常状态:[" + state + "] 操作符: [,] 文本:" + context);
                    }
                    name.delete(0, name.length());
                }
            }
            else
            if (chr == '['){
                switch (state){
                    case FIRST:
                        fathers[step].json = false;
                        if (name.length() > 0){
                            fathers[step].planes.add(name.toString());
                        }
                        state = STATE.LOOP;
                        break;
                    case BUILD:
                        fathers[step].json = false;
                        state = STATE.LOOP;
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['['] 文本:" + context);
                }
                name.delete(0, name.length());
            }
            else
            if (chr == ']'){
                switch (state){
                    case LOOP:
                        if (step > 0){
                            fathers[step - 1].blendObjects.add(fathers[step]);
                            fathers[step--] = null;
                        }
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: [']'] 文本:" + context);
                }
                name.delete(0, name.length());
            }
            else if(chr == '('){
                switch (state){
                    case LOOP:
                        DefaultBlent Blent = new DefaultBlent();
                        fathers[++step] = Blent;
                        Blent.planes.add(name.toString());
                        state = STATE.BUILD;
                        break;
                    case BUILD:
                        fathers[step].planes.add(name.toString());
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['('] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if(chr == ')'){
                switch (state){
                    case BUILD:
                        fathers[step].alias = name.toString();
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: [')'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if (chr == '{'){
                switch (state){
                    case FIRST:
                        fathers[step].json = true;
                        if (name.length() > 0){
                            fathers[step].planes.add(name.toString());
                        }
                        state = STATE.LOOP;
                        break;
                    case LOOP:
                        DefaultBlent Blent = new DefaultBlent();
                        fathers[++step] = Blent;
                        Blent.planes.add(name.toString());
                        state = STATE.BUILD;
                    case BUILD:
                        fathers[step].json = true;
                        state = STATE.LOOP;
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['{'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if (chr == '}'){
                switch (state){
                    case LOOP:
                        if (step > 0){
                            fathers[step - 1].blendObjects.add(fathers[step]);
                            fathers[step--] = null;
                        }
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['}'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else {
                name.append(chr);
            }
        }
        return blent;
    }
    
}

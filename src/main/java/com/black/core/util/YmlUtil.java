package com.black.core.util;

import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class YmlUtil {


    public static Map<String,String> getYmlByFileName(@NonNull String fileName){
        Map<String, String> result = new HashMap<>();
        InputStream in = YmlUtil.class.getClassLoader().getResourceAsStream(fileName);
        Yaml props = new Yaml();
        if (in == null){
            throw new IllegalStateException("no resource of: " + fileName);
        }
        Map<String, Object> obj = props.loadAs(in,Map.class);
        if (obj != null){
            for(Map.Entry<String,Object> entry : obj.entrySet()){
                String key = entry.getKey();
                Object val = entry.getValue();
                if(val instanceof Map){
                    forEachYaml(key, (Map<String, Object>) val, result);
                }else{
                    result.put(key,val.toString());
                }
            }
        }
        return result;
    }

    public static void forEachYaml(String keyStr, Map<String, Object> obj, Map<String, String> result){
        for(Map.Entry<String,Object> entry : obj.entrySet()){
            String key = entry.getKey();
            Object val = entry.getValue();
            String str_new ;
            if(keyStr != null && !"".equals(keyStr)){
                str_new = keyStr + "." + key;
            }else{
                str_new = key;
            }
            if(val instanceof Map){
                forEachYaml(str_new, (Map<String, Object>) val, result);
            }else{
                if (val != null){
                    result.put(str_new,val.toString());
                }

            }
        }
    }
}

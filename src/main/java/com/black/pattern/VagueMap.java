package com.black.pattern;

import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Body;
import com.black.utils.IdUtils;

import java.security.KeyStore;
import java.util.*;

/**
 * @author 李桂鹏
 * @create 2023-07-04 15:44
 */
@SuppressWarnings("all")
public class VagueMap extends Body {

    @Override
    public VagueMap put(String key, Object value) {
        return (VagueMap) super.put(key, value);
    }

    @Override
    public VagueMap putAll0(Map<? extends String, ?> map) {
        return (VagueMap) super.putAll0(map);
    }

    public Collection<Object> vagueQuery(String key){
        List<Object> result = new ArrayList<>();
        Set<String> keySet = keySet();
        int length = key.length();
        for (String k : keySet) {
            int kl = k.length();
            if (length > kl) continue;
            if (Objects.equals(key, k)){
                result.add(get(k));
                continue;
            }

            if (k.contains(key)){
                result.add(get(k));
                continue;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        VagueMap map = new VagueMap();
        for (int i = 0; i < 100000; i++) {
            String id = IdUtils.createShort8Id();
            map.put(id, id);
        }
        ApplicationUtil.programRunMills(() -> {
            System.out.println(map.vagueQuery("h").size());
        });

    }
}

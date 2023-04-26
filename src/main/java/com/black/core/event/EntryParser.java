package com.black.core.event;

import java.util.HashSet;
import java.util.Set;

public class EntryParser {


    public String[] resolver(Set<String> source, String target){
        if (!target.contains("*")){
            return new String[]{target};
        }
        String[] os = target.split("\\*");
        if (os.length == 0){
            return source.toArray(new String[0]);
        }
        if (os.length > 2){
            throw new RuntimeException("目前只能解析含一个 * 的式子");
        }
        Set<String> set = new HashSet<>();
        if (os.length == 2){
            String prefix = os[0];
            String suffix = os[1];
            for (String t : source) {
                if (t.startsWith(prefix) && t.endsWith(suffix)){
                    set.add(t);
                }
            }
        }else {
            //*xx 或者 xx*
            String tr = os[0];
            for (String t : source) {
                if (target.startsWith("*")){
                    if (t.endsWith(tr)){
                        set.add(t);
                    }
                }else if (target.endsWith("*")){
                    if (t.startsWith(tr)){
                        set.add(t);
                    }
                }
            }
        }
        return set.toArray(new String[0]);
    }


}

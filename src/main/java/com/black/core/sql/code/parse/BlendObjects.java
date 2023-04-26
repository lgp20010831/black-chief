package com.black.core.sql.code.parse;

import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter @ToString
public class BlendObjects {

    String name;
    List<String> attributes = new ArrayList<>();
    Map<String, BlendObjects> blendObjects = new ConcurrentHashMap<>();

    public BlendObjects(String name) {
        this.name = name;
    }

    public void add(BlendObjects blendObjects){
        if (blendObjects != null){
            this.blendObjects.put(blendObjects.name, blendObjects);
        }
    }

    public boolean containChild(String name){
        return blendObjects.containsKey(name);
    }

    public BlendObjects getChild(String name){
        return blendObjects.get(name);
    }

    public void add(String an){
        if (StringUtils.hasText(an)){
            attributes.add(an);
        }
    }
}

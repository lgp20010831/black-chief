package com.black.core.sql.code.parse;

import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @ToString
public class BlendObject {

    String name;
    List<String> attributes = new ArrayList<>();

    public BlendObject(String name) {
        this.name = name;
    }

    public void add(String an){
        if (StringUtils.hasText(an)){
            attributes.add(an);
        }
    }
}

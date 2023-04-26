package com.black.premission;

import com.black.JsonBean;

import java.util.Map;

public class JsonAttributeBean extends JsonBean implements Attribute{

    @Override
    public Map<String, Object> attributes() {
        return toJson();
    }

}

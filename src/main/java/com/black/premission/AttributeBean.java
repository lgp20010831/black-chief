package com.black.premission;

import com.black.core.tools.BaseBean;

import java.util.HashMap;
import java.util.Map;

public class AttributeBean<T> extends BaseBean<T> implements Attribute{

    private Map<String, Object> source;

    public AttributeBean(){
        this(new HashMap<>());
    }

    public AttributeBean(Map<String, Object> source){
        this.source = source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    @Override
    public Map<String, Object> attributes() {
        return source;
    }

}

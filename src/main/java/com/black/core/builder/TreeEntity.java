package com.black.core.builder;

import java.util.List;
import java.util.Map;

public class TreeEntity {

    final TreeBuilder.TreeConfig<?> config;

    final Map<String, Object> data;

    public TreeEntity(TreeBuilder.TreeConfig<?> config, Map<String, Object> data) {
        this.config = config;
        this.data = data;
    }

    public Object getId(){
        return getData().get(config.id);
    }

    public Object getPid(){
        return getData().get(config.pid);
    }

    public List<TreeEntity> getChild(){
        return (List<TreeEntity>) getData().get(config.childrenName);
    }

    public Map<String, Object> getData() {
        return data;
    }
}

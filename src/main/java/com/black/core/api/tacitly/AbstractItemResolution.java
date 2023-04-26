package com.black.core.api.tacitly;

import com.black.core.api.handler.ItemResolutionModule;
import com.black.core.util.Av0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractItemResolution implements ItemResolutionModule {

    protected final String name;

    protected List<ItemResolutionModule> lowLevel = new ArrayList<>();

    protected List<Class<?>> dependencyClasses = new ArrayList<>();

    protected List<String> dependencyFields = new ArrayList<>();

    public AbstractItemResolution(String name) {
        this.name = name;
    }

    @Override
    public List<Class<?>> dependencyClasses() {
        return dependencyClasses;
    }

    @Override
    public List<String> dependencyFields() {
        return dependencyFields;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<ItemResolutionModule> lowLevel() {
        return lowLevel;
    }

    protected Map<String, String> parseLevelItem(String item){
        int start = item.indexOf("{");
        int end = item.indexOf("}");
        if (end == -1 || start == -1){
            throw new RuntimeException("条目下级描述符应该包含起始符和结束符: " + item);
        }
        return Av0.of(item.substring(0, start), item.substring(start + 1, end));
    }
}

package com.black.model;

import java.util.HashMap;
import java.util.Map;

public class ModelFactory {

    public static Model createModel(Map<String, Object> map){
        return new ModelImpl(map == null ? new HashMap<>() : map);
    }

}

package com.black.ods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OdsExecuteResult {

    private final List<Map<String, Object>> result;

    public OdsExecuteResult(){
        result = new ArrayList<>();
    }

    public OdsExecuteResult(List<Map<String, Object>> result) {
        this.result = result;
    }

    public void append(List<Map<String, Object>> ap){
        result.addAll(ap);
    }

    public boolean isEmtryResult(){
        return result == null;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }


}

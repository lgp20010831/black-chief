package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.JsonAttributeBean;
import com.black.user.Identity;

import java.util.HashMap;
import java.util.Map;

public class AbstractRUPBean extends JsonAttributeBean implements Identity {

    Map<String, Object> source;

    public AbstractRUPBean(){
        this(null);
    }

    public AbstractRUPBean(Map<String, Object> source) {
        this.source = source == null ? new HashMap<>() : source;
    }

    public void setSource(Map<String, Object> source) {
        this.source = source;
    }

    @Override
    public Map<String, Object> attributes() {
        return source;
    }


    @Override
    public String getName() {
        return getString(GlobalVariablePool.RUP_NAME);
    }

    @Override
    public String getId() {
        return getString(GlobalVariablePool.RUP_ID_NAME);
    }
}

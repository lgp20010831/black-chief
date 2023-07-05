package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.premission.Role;

import java.util.Map;

public class RoleRewriteBean implements Role {
    @Override
    public Map<String, Object> attributes() {
        return JsonUtils.letJson(this);
    }

    @Override
    public String getId() {
        return getString(GlobalVariablePool.RUP_ID_NAME);
    }

    @Override
    public String getName() {
        return getString(GlobalVariablePool.RUP_NAME);
    }

}

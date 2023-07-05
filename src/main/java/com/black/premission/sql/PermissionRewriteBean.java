package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.premission.Permission;
import com.black.sql_v2.Sql;
import com.black.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

public class PermissionRewriteBean implements Permission {
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

    @Override
    public Permission father() {
        return Sql.queryById(GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME, getString(GlobalVariablePool.RUP_PERMISSION_FATHER_NAME)).javaSingle(getClass());
    }

    @Override
    public List<Permission> childs() {
        Map<String, String> condition = ServiceUtils.ofMap(GlobalVariablePool.RUP_PERMISSION_FATHER_NAME, getId());
        Object permissionRewriteBeans = Sql.query(GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME, condition).javaList(getClass());
        return (List<Permission>) permissionRewriteBeans;
    }
}

package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.premission.Permission;
import com.black.premission.RoleWithPermission;
import com.black.premission.collect.SqlCollector;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class RoleWithPermissionRewriteBean implements RoleWithPermission {
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
    public List<? extends Permission> getPremissions() {
        SqlCollector.RUPEntityManager manager = SqlCollector.RUPEntityManager.getInstance();
        Class<? extends Permission> permissionEntity = manager.getPermissionEntity();
        return getList(GlobalVariablePool.RUP_PERMISSION_LIST_NAME, permissionEntity);
    }
}

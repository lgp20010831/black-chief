package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.Permission;
import com.black.premission.RoleWithPermission;

import java.util.List;

public class RolePermissionMapBean extends AbstractRUPBean implements RoleWithPermission {
    @Override
    public List<Permission> getPremissions() {
        return getList(GlobalVariablePool.RUP_PERMISSION_LIST_NAME, Permission.class);
    }
}

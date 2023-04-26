package com.black.premission.map_sql;

import com.black.premission.Permission;

import java.util.List;

import static com.black.GlobalVariablePool.RUP_PERMISSION_CHILDREN_NAME;
import static com.black.GlobalVariablePool.RUP_PERMISSION_FATHER_NAME;

public class PermissionMapBean extends AbstractRUPBean implements Permission {


    @Override
    public Permission father() {
        return getObject(RUP_PERMISSION_FATHER_NAME, Permission.class);
    }

    @Override
    public List<Permission> childs() {
        return getList(RUP_PERMISSION_CHILDREN_NAME, Permission.class);
    }
}

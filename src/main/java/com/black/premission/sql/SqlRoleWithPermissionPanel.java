package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.util.StreamUtils;
import com.black.premission.*;
import com.black.premission.collect.SqlCollector;
import com.black.sql_v2.Sql;
import com.black.utils.CollectionUtils;
import com.black.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface SqlRoleWithPermissionPanel<P extends RoleWithPermission> extends RolePermissionPanel<P>, SqlPanel<P> {

    @Override
    default String getTableName() {
        return GlobalVariablePool.RUP_MS_ROLE_PERMISSION_TABLE_NAME;
    }

    @Override
    default Class entityType(){
        return SqlCollector.RUPEntityManager.getInstance().getRoleWithPermissionEntity();
    }

    @Override
    default List<Permission> getPremission(String roleId){
        SqlCollector.RUPEntityManager manager = SqlCollector.RUPEntityManager.getInstance();
        Class<? extends Permission> permissionEntity = manager.getPermissionEntity();
        Class<? extends RoleWithPermission> roleWithPermissionEntity = manager.getRoleWithPermissionEntity();
        Map<String, String> condition = ServiceUtils.ofMap(GlobalVariablePool.RUP_ROLE_ID_NAME, roleId);
        List<? extends RoleWithPermission> roleWithPermissions = Sql.query(GlobalVariablePool.RUP_MS_ROLE_PERMISSION_TABLE_NAME, condition).javaList(roleWithPermissionEntity);
        List<String> permissionIds = StreamUtils.mapList(roleWithPermissions, rwp -> rwp.getString(GlobalVariablePool.RUP_PERMISSION_ID_NAME));
        if (CollectionUtils.isEmpty(permissionIds)){
            return new ArrayList<>();
        }
        Object permissions = Sql.queryById(GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME, permissionIds).javaList(permissionEntity);
        return (List<Permission>) permissions;
    }

}

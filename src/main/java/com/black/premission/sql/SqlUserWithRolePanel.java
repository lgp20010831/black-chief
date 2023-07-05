package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.util.StreamUtils;
import com.black.premission.Role;
import com.black.premission.RoleUserPanel;
import com.black.premission.UserWithRole;
import com.black.premission.collect.SqlCollector;
import com.black.sql_v2.Sql;
import com.black.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

public interface SqlUserWithRolePanel<P extends UserWithRole> extends RoleUserPanel<P>, SqlPanel<P> {

    @Override
    default String getTableName() {
        return GlobalVariablePool.RUP_MS_USER_ROLE_TABLE_NAME;
    }

    @Override
    default Class entityType(){
        return SqlCollector.RUPEntityManager.getInstance().getUserWithRoleEntity();
    }

    @Override
    default List<Role> getRoles(String userId){
        SqlCollector.RUPEntityManager manager = SqlCollector.RUPEntityManager.getInstance();
        Class<? extends Role> roleEntity = manager.getRoleEntity();
        Class<? extends UserWithRole> userWithRoleEntity = manager.getUserWithRoleEntity();
        Map<String, String> condition = ServiceUtils.ofMap(GlobalVariablePool.RUP_USER_ID_NAME, userId);
        List<? extends UserWithRole> userWithRoles = Sql.query(GlobalVariablePool.RUP_MS_USER_ROLE_TABLE_NAME, condition).javaList(userWithRoleEntity);
        List<String> roleIds = StreamUtils.mapList(userWithRoles, uwr -> uwr.getString(GlobalVariablePool.RUP_ROLE_ID_NAME));
        Object roles = Sql.queryById(GlobalVariablePool.RUP_MS_ROLE_TABLE_NAME, roleIds).javaList(roleEntity);
        return (List<Role>) roles;
    }
}

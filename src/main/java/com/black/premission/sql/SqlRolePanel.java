package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.premission.Role;
import com.black.premission.RolePanel;
import com.black.premission.collect.SqlCollector;

public interface SqlRolePanel<P extends Role> extends RolePanel<P>, SqlPanel<P> {

    @Override
    default String getTableName() {
        return GlobalVariablePool.RUP_MS_ROLE_TABLE_NAME;
    }

    @Override
    default Class entityType(){
        return SqlCollector.RUPEntityManager.getInstance().getRoleEntity();
    }
}

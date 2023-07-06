package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.premission.Permission;
import com.black.premission.PermissionPanel;
import com.black.premission.collect.SqlCollector;

public interface SqlPermissionPanel<P extends Permission> extends PermissionPanel<P>, SqlPanel<P> {

    @Override
    default String getTableName() {
        return GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME;
    }

    @Override
    default Class entityType(){
        return SqlCollector.RUPEntityManager.getInstance().getPermissionEntity();
    }
}

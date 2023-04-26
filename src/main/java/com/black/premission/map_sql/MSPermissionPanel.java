package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.PermissionPanel;

public interface MSPermissionPanel extends MSPanel<PermissionMapBean>, PermissionPanel<PermissionMapBean> {

    @Override
    default String getTableName(){
        return GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME;
    }


    @Override
    default Class<PermissionMapBean> entityType() {
        return PermissionMapBean.class;
    }
}

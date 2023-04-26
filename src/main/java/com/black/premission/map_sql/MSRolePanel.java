package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.RolePanel;

public interface MSRolePanel extends MSPanel<RoleMapBean>, RolePanel<RoleMapBean> {

    @Override
    default String getTableName(){
        return GlobalVariablePool.RUP_MS_ROLE_TABLE_NAME;
    }

    @Override
    default Class<RoleMapBean> entityType() {
        return RoleMapBean.class;
    }
}

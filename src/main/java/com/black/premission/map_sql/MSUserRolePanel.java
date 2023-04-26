package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.*;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.StreamUtils;
import com.black.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

public interface MSUserRolePanel extends MSPanel<UserRoleMapBean>, RoleUserPanel<UserRoleMapBean> {

    @Override
    default String getTableName(){
        return GlobalVariablePool.RUP_MS_USER_ROLE_TABLE_NAME;
    }

    @Override
    default Class<UserRoleMapBean> entityType() {
        return UserRoleMapBean.class;
    }

    @Override
    default List<Role> getRoles(String userId){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        RolePanel<Role> rolePanel = configuration.getRolePanel();
        List<Map<String, Object>> maps = mapping.globalSelect(getTableName(), ServiceUtils.ofMap(GlobalVariablePool.RUP_USER_ID_NAME, userId));
        return StreamUtils.mapList(maps, map -> {
            String rid = ServiceUtils.getString(map, GlobalVariablePool.RUP_ROLE_ID_NAME);
            if (rid != null){
                return rolePanel.findDataById(rid);
            }
            return null;
        });
    }
}

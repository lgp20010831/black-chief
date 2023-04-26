package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.*;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.StreamUtils;
import com.black.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

public interface MSRolePermissionPanel extends MSPanel<RolePermissionMapBean>, RolePermissionPanel<RolePermissionMapBean> {

    @Override
    default String getTableName(){
        return GlobalVariablePool.RUP_MS_ROLE_PERMISSION_TABLE_NAME;
    }


    @Override
    default List<Permission> getPremission(String roleId){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        PermissionPanel<Permission> premissionPanel = configuration.getPremissionPanel();
        List<Map<String, Object>> maps = mapping.globalSelect(getTableName(), ServiceUtils.ofMap(GlobalVariablePool.RUP_ROLE_ID_NAME, roleId));
        return StreamUtils.mapList(maps, map -> {
            String pid = ServiceUtils.getString(map, GlobalVariablePool.RUP_PERMISSION_ID_NAME);
            if (pid != null){
                return premissionPanel.findDataById(pid);
            }
            return null;
        });
    }

    @Override
    default Class<RolePermissionMapBean> entityType() {
        return RolePermissionMapBean.class;
    }
}

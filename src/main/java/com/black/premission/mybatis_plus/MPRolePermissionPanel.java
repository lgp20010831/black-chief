package com.black.premission.mybatis_plus;


import com.black.GlobalVariablePool;
import com.black.core.util.StreamUtils;
import com.black.premission.*;
import com.black.utils.ReflexHandler;
import com.black.utils.ServiceUtils;

import java.util.List;

public interface MPRolePermissionPanel<R extends RoleWithPermission> extends MPPanel<R>, RolePermissionPanel<R> {

    @Override
    default Class<R> entityType(){
        Class<? extends MPRolePermissionPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, MPRolePermissionPanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

    @Override
    default List<Permission> getPremission(String roleId){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        R instance = instance(ServiceUtils.ofMap(GlobalVariablePool.RUP_ROLE_ID_NAME, roleId));
        PermissionPanel<Permission> premissionPanel = configuration.getPremissionPanel();
        List<R> rs = dataList(instance);
        return StreamUtils.mapList(rs, map -> {
            String pid = ServiceUtils.getString(map, GlobalVariablePool.RUP_PERMISSION_ID_NAME);
            if (pid != null){
                return premissionPanel.findDataById(pid);
            }
            return null;
        });
    }
}

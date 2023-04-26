package com.black.premission.jpa;

import com.black.GlobalVariablePool;
import com.black.core.util.StreamUtils;
import com.black.premission.*;
import com.black.utils.ReflexHandler;
import com.black.utils.ServiceUtils;

import java.util.List;


public interface JPARoleUserPanel<R extends UserWithRole> extends JPAPanel<R>, RoleUserPanel<R> {


    @Override
    default Class<R> entityType(){
        Class<? extends JPARoleUserPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, JPARoleUserPanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

    @Override
    default List<Role> getRoles(String userId){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        R instance = instance(ServiceUtils.ofMap(GlobalVariablePool.RUP_USER_ID_NAME, userId));
        RolePanel<Role> rolePanel = configuration.getRolePanel();
        List<R> rs = dataList(instance);
        return StreamUtils.mapList(rs, map -> {
            String rid = ServiceUtils.getString(map, GlobalVariablePool.RUP_ROLE_ID_NAME);
            if (rid != null){
                return rolePanel.findDataById(rid);
            }
            return null;
        });
    }
}

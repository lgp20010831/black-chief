package com.black.premission.mybatis_plus;


import com.black.premission.Role;
import com.black.premission.RolePanel;
import com.black.utils.ReflexHandler;


public interface MPRolePanel<R extends Role> extends MPPanel<R>, RolePanel<R> {

    @Override
    default Class<R> entityType(){
        Class<? extends MPRolePanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, MPRolePanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }


}

package com.black.premission.mybatis_plus;


import com.black.premission.Permission;
import com.black.premission.PermissionPanel;
import com.black.utils.ReflexHandler;


public interface MPPermissionPanel<R extends Permission> extends MPPanel<R>, PermissionPanel<R> {

    @Override
    default Class<R> entityType(){
        Class<? extends MPPermissionPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, MPPermissionPanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }


}

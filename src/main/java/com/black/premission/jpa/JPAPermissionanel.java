package com.black.premission.jpa;

import com.black.premission.Permission;
import com.black.premission.PermissionPanel;
import com.black.utils.ReflexHandler;


public interface JPAPermissionanel<R extends Permission> extends JPAPanel<R>, PermissionPanel<R> {


    @Override
    default Class<R> entityType(){
        Class<? extends JPAPermissionanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, JPAPermissionanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

}

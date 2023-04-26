package com.black.premission.jpa;

import com.black.premission.Role;
import com.black.premission.RolePanel;
import com.black.utils.ReflexHandler;


public interface JPARolePanel<R extends Role> extends JPAPanel<R>, RolePanel<R>{


    @Override
    default Class<R> entityType(){
        Class<? extends JPARolePanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, JPARolePanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

}

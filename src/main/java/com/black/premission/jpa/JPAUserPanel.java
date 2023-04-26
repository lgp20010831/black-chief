package com.black.premission.jpa;


import com.black.user.User;
import com.black.user.UserPanel;
import com.black.utils.ReflexHandler;


public interface JPAUserPanel<R extends User> extends JPAPanel<R>, UserPanel<R> {


    @Override
    default Class<R> entityType(){
        Class<? extends JPAUserPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, JPAUserPanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }

}

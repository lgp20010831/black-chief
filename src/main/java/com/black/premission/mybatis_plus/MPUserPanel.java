package com.black.premission.mybatis_plus;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.black.user.User;
import com.black.user.UserPanel;
import com.black.utils.ReflexHandler;

public interface MPUserPanel<R extends User> extends MPPanel<R>, UserPanel<R> {

    @Override
    default Class<R> entityType(){
        Class<? extends MPUserPanel> type = getClass();
        Class<?>[] genericVals = ReflexHandler.genericVal(type, MPUserPanel.class);
        if (genericVals.length != 1){
            throw new IllegalStateException("can not find generic type of class: " + type);
        }
        return (Class<R>) genericVals[0];
    }


    default R getUser(String account){
        return selectOne(new QueryWrapper<R>().eq(getAccountName(), account));
    }

    default boolean updateUser(String oldAccount, R newUser){
        return update(newUser, new QueryWrapper<R>().eq(getAccountName(), oldAccount)) > 0;
    }

    default boolean joinUser(R user){
        return insert(user) > 0;
    }

    default boolean deleteUser(String a){
        return delete(new QueryWrapper<R>().eq(getAccountName(), a)) > 0;
    }

}

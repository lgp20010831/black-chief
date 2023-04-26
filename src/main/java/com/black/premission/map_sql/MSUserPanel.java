package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.user.UserPanel;
import com.black.utils.ServiceUtils;

import java.util.Collections;
import java.util.Map;

public interface MSUserPanel extends MSPanel<UserRoleMapBean>, UserPanel<UserRoleMapBean> {

    @Override
    default String getTableName(){
        return GlobalVariablePool.RUP_MS_USER_TABLE_NAME;
    }

    @Override
    default Class<UserRoleMapBean> entityType() {
        return UserRoleMapBean.class;
    }

    @Override
    default UserRoleMapBean getUser(String account){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        Map<String, Object> map = mapping.globalSelectSingle(getTableName(), ServiceUtils.ofMap(getAccountName(), account));
        UserRoleMapBean userRoleMapBean = new UserRoleMapBean();
        userRoleMapBean.setSource(map);
        return userRoleMapBean;
    }

    @Override
    default boolean updateUser(String oldAccount, UserRoleMapBean newUser){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        mapping.globalUpdate(getTableName(), newUser.toJson(), ServiceUtils.ofMap(getAccountName(), oldAccount));
        return true;
    }

    @Override
    default boolean joinUser(UserRoleMapBean user){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        return mapping.fastJoin(getTableName(), Collections.singletonList(user.toJson()));
    }

    @Override
    default boolean deleteUser(String a){
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        GlobalParentMapping mapping = configuration.getParentMapping();
        return mapping.deleteById(getTableName(), a);
    }
}

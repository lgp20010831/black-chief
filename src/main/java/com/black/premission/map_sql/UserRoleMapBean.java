package com.black.premission.map_sql;

import com.black.GlobalVariablePool;
import com.black.premission.Role;
import com.black.premission.UserWithRole;

import java.util.List;

public class UserRoleMapBean extends AbstractRUPBean implements UserWithRole {

    @Override
    public List<Role> getRoles() {
        return getList(GlobalVariablePool.RUP_ROLE_LIST_NAME, Role.class);
    }

    @Override
    public String getAccount() {
        return getString(GlobalVariablePool.RUP_USER_ACCOUNT_NAME);
    }

    @Override
    public String getPassword() {
        return getString(GlobalVariablePool.RUP_USER_PASSWORD_NAME);
    }

    @Override
    public void setPassword(String password) {
        put(GlobalVariablePool.RUP_USER_PASSWORD_NAME, password);
    }
}

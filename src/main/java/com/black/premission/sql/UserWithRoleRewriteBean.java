package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.premission.Role;
import com.black.premission.UserWithRole;
import com.black.premission.collect.SqlCollector;
import com.black.utils.ServiceUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class UserWithRoleRewriteBean implements UserWithRole {
    @Override
    public Map<String, Object> attributes() {
        return JsonUtils.letJson(this);
    }

    @Override
    public String getId() {
        return getString(GlobalVariablePool.RUP_ID_NAME);
    }

    @Override
    public String getName() {
        return getString(GlobalVariablePool.RUP_NAME);
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
        ServiceUtils.setProperty(this, GlobalVariablePool.RUP_USER_PASSWORD_NAME, password);
    }

    @Override
    public List<? extends Role> obtainRoles() {
        SqlCollector.RUPEntityManager manager = SqlCollector.RUPEntityManager.getInstance();
        Class<? extends Role> roleEntity = manager.getRoleEntity();
        return getList(GlobalVariablePool.RUP_ROLE_LIST_NAME, roleEntity);
    }
}

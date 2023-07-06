package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.user.User;
import com.black.utils.ServiceUtils;

import java.util.Map;

public class UserRewriteBean implements User {
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
}

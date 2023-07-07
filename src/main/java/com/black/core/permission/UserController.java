package com.black.core.permission;

import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.util.Assert;
import com.black.pattern.PremiseProxy;
import com.black.role.SkipVerification;
import com.black.user.User;
import com.black.user.UserPanel;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@CrossOrigin
@RequestMapping("rup/user") @SkipVerification
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @Api(tags = "RUP用户管理模块")
public class UserController extends AbstractRUPController<User, UserPanel<User>> {

    @Override
    protected Object doJoinData(JSONObject json) {
        String account = json.getString(GlobalVariablePool.RUP_USER_ACCOUNT_NAME);
        Assert.notNull(account, "账号不能为空");
        User old = find().getUser(account);
        Assert.falseThrows(old == null, "当前用户已经存在: " + account);
        return super.doJoinData(json);
    }

    @Override
    protected Object doSaveData(JSONObject json) {
        return super.doSaveData(json);
    }
}

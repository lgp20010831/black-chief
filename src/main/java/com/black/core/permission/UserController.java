package com.black.core.permission;

import com.black.api.ApiRemark;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.pattern.PremiseProxy;
import com.black.user.User;
import com.black.user.UserPanel;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@CrossOrigin
@RequestMapping("user")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @ApiRemark("用户操作接口")
public class UserController extends AbstractRUPController<User, UserPanel<User>> {


}

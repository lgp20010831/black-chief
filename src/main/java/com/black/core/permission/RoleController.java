package com.black.core.permission;

import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.pattern.PremiseProxy;
import com.black.premission.Role;
import com.black.premission.RolePanel;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@CrossOrigin
@RequestMapping("rup/role")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @Api(tags = "RUP角色管理模块")
public class RoleController extends AbstractRUPController<Role, RolePanel<Role>> {


}

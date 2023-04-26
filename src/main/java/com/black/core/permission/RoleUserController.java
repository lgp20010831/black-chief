package com.black.core.permission;

import com.black.api.ApiJdbcProperty;
import com.black.api.ApiRemark;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.pattern.PremiseProxy;
import com.black.premission.RoleUserPanel;
import com.black.premission.UserWithRole;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@CrossOrigin
@RequestMapping("roleUser")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @ApiRemark("用户角色操作接口")
public class RoleUserController extends AbstractRUPController<UserWithRole, RoleUserPanel<UserWithRole>> {


    @RUPServletMethod
    @GetMapping("getRoles")
    @ApiJdbcProperty(request = "url: ?userId=用户id", response = "$<getTableName>[]", remark = "根据用户id获取所属的角色")
    public Object getRoles(@RequestParam String userId){
        RoleUserPanel<UserWithRole> roleUserPanel = find();
        return roleUserPanel.getRoles(userId);
    }


}

package com.black.core.permission;

import com.black.api.ApiJdbcProperty;
import com.black.api.ApiRemark;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.pattern.PremiseProxy;
import com.black.premission.RolePermissionPanel;
import com.black.premission.RoleWithPermission;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@CrossOrigin
@RequestMapping("rolePermission")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @ApiRemark("用户权限操作接口")
public class RolePermissionController extends AbstractRUPController<RoleWithPermission, RolePermissionPanel<RoleWithPermission>> {

    @RUPServletMethod
    @GetMapping("getPermissions")
    @ApiJdbcProperty(request = "url: ?roleId=角色id", response = "$<getTableName>[]", remark = "根据角色id获取该角色所拥有的权限")
    public Object getPermissions(@RequestParam String roleId){
        RolePermissionPanel<RoleWithPermission> rolePermissionPanel = find();
        return rolePermissionPanel.getPremission(roleId);
    }

}

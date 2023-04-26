package com.black.core.permission;

import com.black.api.ApiJdbcProperty;
import com.black.api.ApiRemark;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.pattern.PremiseProxy;
import com.black.premission.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@CrossOrigin
@RequestMapping("permission")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @ApiRemark("权限操作接口")
public class PermissionController extends AbstractRUPController<Permission, PermissionPanel<Permission>> {

    @RUPServletMethod
    @GetMapping("getPermissionByUserId")
    @ApiJdbcProperty(request = "url: ?userId=用户id", response = "$<getTableName>[]", remark = "根据用户id获取该用户所开启的权限")
    public Object getPermissionByUserId(@RequestParam String userId){
        RoleUserPanel<UserWithRole> roleUserPanel = find(RoleUserPanel.class);
        List<Role> roles = roleUserPanel.getRoles(userId);
        RolePermissionPanel<RoleWithPermission> rolePermissionPanel = find(RolePermissionPanel.class);
        List<Permission> permissions = new ArrayList<>();
        for (Role role : roles) {
            permissions.addAll(rolePermissionPanel.getPremission(role.getId()));
        }
        return permissions;
    }

}

package com.black.core.permission;

import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.api.ApiJdbcProperty;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.builder.TreeBuilder;
import com.black.core.sql.code.util.SQLUtils;
import com.black.pattern.PremiseProxy;
import com.black.premission.*;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@CrossOrigin
@RequestMapping("rup/permission")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @Api(tags = "RUP权限管理模块")
public class PermissionController extends AbstractRUPController<Permission, PermissionPanel<Permission>> {

    @RUPServletMethod
    @GetMapping("getPermissionByUserId")
    @ApiOperation("根据用户id获取该用户所开启的权限")
    @V2Swagger("$<getTableName>{}")
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



    @Override
    protected Object doGetList(JSONObject json) {
        Object list = super.doGetList(json);
        List<Object> objects = SQLUtils.wrapList(list);
        return TreeBuilder.prepare(objects).pid(GlobalVariablePool.RUP_PERMISSION_FATHER_NAME).exceute();
    }

}

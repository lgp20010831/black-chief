package com.black.core.permission;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.api.ApiJdbcProperty;
import com.black.core.annotation.ChiefTransaction;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.util.Body;
import com.black.pattern.PremiseProxy;
import com.black.premission.RolePermissionPanel;
import com.black.premission.RoleWithPermission;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@CrossOrigin
@RequestMapping("rup/rolePermission")
@PremiseProxy(EnabledPermissionPremise.class) @ChiefTransaction
@GlobalEnhanceRestController @Api(tags = "RUP角色权限管理模块")
public class RolePermissionController extends AbstractRUPController<RoleWithPermission, RolePermissionPanel<RoleWithPermission>> {

    @RUPServletMethod
    @GetMapping("getPermissions")
    @V2Swagger("$<getPermissionTableName>{}")
    @ApiOperation("根据角色id获取该角色所拥有的权限")
    @ApiJdbcProperty(request = "url: ?roleId=角色id", response = "$<getTableName>[]", remark = "根据角色id获取该角色所拥有的权限")
    public Object getPermissions(@RequestParam String roleId){
        RolePermissionPanel<RoleWithPermission> rolePermissionPanel = find();
        return rolePermissionPanel.getPremission(roleId);
    }

    public String getPermissionTableName(){
        return GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME;
    }

    @RUPServletMethod
    @ApiOperation("给角色配置权限")
    @PostMapping("configPermissionToRole")
    public void configPermissionToRole(@V2Swagger("{roleId:角色id,permissionIds:权限id列表}") @RequestBody JSONObject json){
        String roleId = json.getString("roleId");
        JSONArray permissionIds = json.getJSONArray("permissionIds");
        RolePermissionPanel<RoleWithPermission> panel = find();
        for (Object permissionId : permissionIds) {
            Body body = new Body().putAll0(json)
                    .put(GlobalVariablePool.RUP_ROLE_ID_NAME, roleId)
                    .put(GlobalVariablePool.RUP_PERMISSION_ID_NAME, permissionId);
            panel.join(panel.convert(body));
        }
    }

}

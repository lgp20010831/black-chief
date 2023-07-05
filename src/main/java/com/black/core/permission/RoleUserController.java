package com.black.core.permission;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.api.ApiJdbcProperty;
import com.black.core.annotation.ChiefTransaction;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.util.Body;
import com.black.pattern.PremiseProxy;
import com.black.premission.RoleUserPanel;
import com.black.premission.UserWithRole;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Log4j2
@CrossOrigin
@RequestMapping("rup/roleUser")
@PremiseProxy(EnabledPermissionPremise.class)
@GlobalEnhanceRestController @Api(tags = "RUP角色用户管理模块") @ChiefTransaction
public class RoleUserController extends AbstractRUPController<UserWithRole, RoleUserPanel<UserWithRole>> {


    @RUPServletMethod
    @GetMapping("getRoles")
    @V2Swagger("$<getRoleTableName>{}")
    @ApiOperation("根据用户id获取所属的角色")
    @ApiJdbcProperty(request = "url: ?userId=用户id", response = "$<getRoleTableName>[]", remark = "根据用户id获取所属的角色")
    public Object getRoles(@RequestParam String userId){
        RoleUserPanel<UserWithRole> roleUserPanel = find();
        return roleUserPanel.getRoles(userId);
    }

    public String getRoleTableName(){
        return GlobalVariablePool.RUP_MS_ROLE_TABLE_NAME;
    }

    @RUPServletMethod
    @ApiOperation("给用户配置角色")
    @PostMapping("configRoleToUser")
    public void configRoleToUser(@V2Swagger("{userId:用户id,roleIds:角色id列表}") @RequestBody JSONObject json){
        String userId = json.getString("userId");
        JSONArray roleIds = json.getJSONArray("roleIds");
        RoleUserPanel<UserWithRole> panel = find();
        for (Object roleId : roleIds) {
            Body body = new Body().putAll0(json)
                    .put(GlobalVariablePool.RUP_USER_ID_NAME, userId)
                    .put(GlobalVariablePool.RUP_ROLE_ID_NAME, roleId);
            panel.join(panel.convert(body));
        }
    }
}

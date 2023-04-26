package com.black.premission;

import java.util.List;

public interface RolePermissionPanel<RP extends RoleWithPermission> extends Panel<RP>{

    //根据角色获取其所拥有的权限
    List<Permission> getPremission(String roleId);


}

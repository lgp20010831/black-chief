package com.black.premission;

import java.util.List;

public interface RoleWithPermission extends Role{

    //获取该角色所拥有的权限
    List<? extends Permission> getPremissions();

}

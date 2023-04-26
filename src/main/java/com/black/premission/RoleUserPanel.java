package com.black.premission;


import java.util.List;

public interface RoleUserPanel <RU extends UserWithRole> extends Panel<RU> {

    //获取该用户所属的角色
    List<Role> getRoles(String userId);

}

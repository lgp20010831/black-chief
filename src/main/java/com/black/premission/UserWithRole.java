package com.black.premission;

import com.black.user.User;

import java.util.List;

public interface UserWithRole extends User, Attribute {

    //获取当前用户所属的角色
    List<Role> getRoles();

}

package com.black.premission;

import com.black.user.Identity;

import java.util.List;

public interface Permission extends Attribute, Identity {

    @Override
    String getId();

    @Override
    String getName();

    //获取父权限
    Permission father();

    //获取所有的子权限
    List<Permission> childs();
}

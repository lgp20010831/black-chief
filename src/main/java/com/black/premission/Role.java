package com.black.premission;

import com.black.user.Identity;

public interface Role extends Attribute, Identity {

    @Override
    String getName();

    @Override
    String getId();


}

package com.black.user;

import com.black.premission.Attribute;

public interface User extends Attribute {

    String getAccount();

    String getPassword();

    void setPassword(String password);
}

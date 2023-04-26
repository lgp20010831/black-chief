package com.black.role;

import com.black.user.User;

public interface TokenCreator {

    String createUserToken(Configuration configuration, User user);

    String createNoseToken(Configuration configuration, User user);
}

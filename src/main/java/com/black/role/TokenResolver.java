package com.black.role;

import com.black.core.servlet.TokenExpirationException;
import com.black.user.User;

import javax.servlet.http.HttpServletRequest;

public interface TokenResolver {

    String getToken(HttpServletRequest request, Configuration configuration);


    User verification(Configuration configuration, String token, HttpServletRequest request) throws TokenExpirationException, TokenInvalidException;

}

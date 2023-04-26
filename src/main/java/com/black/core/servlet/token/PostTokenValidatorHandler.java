package com.black.core.servlet.token;

import com.black.core.servlet.NoTokenException;
import com.black.core.servlet.TokenExpirationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PostTokenValidatorHandler {


    Object whenNoTokenHandler(NoTokenException ex, HttpServletRequest request, HttpServletResponse response, Object handler);

    Object whenTokenExpirationHandler(TokenExpirationException ex, HttpServletRequest request, HttpServletResponse response, Object handler);


}

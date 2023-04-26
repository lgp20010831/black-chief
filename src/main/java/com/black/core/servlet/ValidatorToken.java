package com.black.core.servlet;


import javax.servlet.http.HttpServletRequest;

public interface ValidatorToken {
    /** 验证通过则返回 true, 否则返回 false */
    String validatorToken(TokenResolver resolver, HttpServletRequest request, String URL, String servletPath) throws NoTokenException, TokenExpirationException;
}

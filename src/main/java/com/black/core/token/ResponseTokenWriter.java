package com.black.core.token;

import com.black.role.TokenInvalidException;
import com.black.role.TokenListener;
import com.black.core.mvc.response.Response;
import com.black.core.servlet.HttpResponseUtil;
import com.black.core.servlet.TokenExpirationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static com.black.GlobalVariablePool.*;

public class ResponseTokenWriter implements TokenListener {

    @Override
    public void resloverTokenExpiration(HttpServletRequest request, HttpServletResponse response, Method method, Class<?> beanType, Object bean, TokenExpirationException e) {
        Response res = new Response(HTTP_CODE_TOKEN_EXPIRATION, false, HTTP_MSG_TOKEN_EXPIRATION);
        HttpResponseUtil.writeUtf8JsonResult(res, response);
    }

    @Override
    public void resolverTokenInvaild(HttpServletRequest request, HttpServletResponse response, Method method, Class<?> beanType, Object bean, TokenInvalidException e) {
        Response res = new Response(HTTP_CODE_TOKEN_INVAILD, false, HTTP_MSG_TOKEN_INVAILD);
        HttpResponseUtil.writeUtf8JsonResult(res, response);
    }

    @Override
    public void resolverNoToken(HttpServletRequest request, HttpServletResponse response, Method method, Class<?> beanType, Object bean) {
        Response res = new Response(HTTP_CODE_NO_TOKEN, false, HTTP_MSG_NO_TOKEN);
        HttpResponseUtil.writeUtf8JsonResult(res, response);
    }
}

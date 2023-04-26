package com.black.core.servlet;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.black.core.mvc.response.ResponseUtil;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.servlet.token.PostTokenValidatorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


public interface ValidatorTokenConfigurer {

    /***
     * 处理 token 是否过期的执行者
     * @return 返回一个处理者
     */
    default ValidatorToken giveParser(){
        return (resolver, request, url, servletPath) -> {
            String token = resolver.getToken(request);
            try {
                Date expireAt = resolver.getExpireAt(token);
                if (expireAt != null && expireAt.getTime() < System.currentTimeMillis()){
                    throw new TokenExpirationException();
                }
            }catch (JWTDecodeException de){
                CentralizedExceptionHandling.handlerException(de);
                throw new TokenExpirationException("decode token has error");
            }
            return token;
        };
    }

    default PostTokenValidatorHandler giveTokenHandler(){
        return new PostTokenValidatorHandler() {
            @Override
            public Object whenNoTokenHandler(NoTokenException ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
                return ResponseUtil.invalid();
            }

            @Override
            public Object whenTokenExpirationHandler(TokenExpirationException ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
                return ResponseUtil.invalid();
            }
        };
    }

    default String[] validatorRange(){
        return new String[0];
    }

    default String tokenHeaderString(){
        return "authorization";
    }

    default String tokenPrefix(){
        return "Bearer ";
    }
}

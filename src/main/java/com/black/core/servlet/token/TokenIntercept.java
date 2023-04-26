package com.black.core.servlet.token;

import com.black.core.servlet.*;
import com.black.core.servlet.annotation.UnwantedVerify;
import com.black.core.servlet.annotation.Verify;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Log4j2
public class TokenIntercept implements HandlerInterceptor {

    TokenResolver tokenResolver;

    PostTokenValidatorHandler tokenValidatorHandler;

    Collection<String> validatorRange;

    public TokenIntercept(TokenResolver tokenResolver,
                          PostTokenValidatorHandler postTokenValidatorHandler,
                          Collection<String> validatorRange){
        if (log.isInfoEnabled()) {
            log.info("token validator intercept init ...");
        }
        this.tokenResolver = tokenResolver;
        this.validatorRange = validatorRange;
        this.tokenValidatorHandler = postTokenValidatorHandler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (log.isInfoEnabled()) {
            log.info("请求 URL 地址: {}", request.getRequestURL());
        }
        //如果是一个跨域请求
        if (!CorsUtils.isPreFlightRequest(request)) {
            if (tokenResolver.isCapableOfVerification()){
                if (handler instanceof HandlerMethod){
                    return resolverHandlerMethod((HandlerMethod) handler, request, response);
                }
            }
        }else {
            if (log.isInfoEnabled()) {
                log.info("跨域请求:\n发起请求域: {};\n期望允许的方法: {};\n期望允许的请求头: {};\n",
                        request.getHeader(HttpRequestUtil.CORE_ORIGIN),
                        request.getHeader(HttpRequestUtil.CORS_ACCESS_CONTROL_REQUEST_METHOD),
                        request.getHeader(HttpRequestUtil.CORS_ACCESS_CONTROL_REQUEST_HEADERS));
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        if (CorsUtils.isPreFlightRequest(request)){
            Map<String, String> responseHeadersMap = HttpResponseUtil.getResponseHeadersMap(response);
            if (log.isInfoEnabled()) {
                log.info("对于跨域请求结果: {}; \n响应 headersMap:{}",
                        HttpResponseUtil.CORS_FAIL == response.getStatus() ? "禁止" : "允许", responseHeadersMap);
            }
        }else {
            Object attribute = request.getAttribute(HttpRequestUtil.REQUEST_TOKEN_PARAM);
            if (attribute != null){
                request.removeAttribute(HttpRequestUtil.REQUEST_TOKEN_PARAM);
            }
        }
    }

    /***
     * 将 token 信息存贮到 request 里面
     * 如果控制器想要获取 token: 可以通过两种方式:
     * 1.被Aop所映射, 然后可以标注 {@link com.black.core.servlet.annotation.Token} 注解
     *   那么将会自动注入,
     * 2.可以获取{@link HttpServletRequest} ，然后调用 getAttribute(HttpRequestUtil.REQUEST_TOKEN_PARAM)
     *   来获取
     * @param token token
     * @param request 请求实例
     */
    protected void setToken(String token, HttpServletRequest request){
        request.setAttribute(HttpRequestUtil.REQUEST_TOKEN_PARAM, token);
    }


    protected boolean isVaildRange(String name){
        for (String r : validatorRange) {
            if (name.startsWith(r)){
                return true;
            }
        }
        return false;
    }

    protected boolean resolverHandlerMethod(HandlerMethod handlerMethod,
                                         HttpServletRequest request,
                                         HttpServletResponse response){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(handlerMethod.getBean());
        if (isVaildRange(primordialClass.getName())){
            Method targetMethod = handlerMethod.getMethod();
            boolean globalFilter = AnnotationUtils.getAnnotation(primordialClass, UnwantedVerify.class) != null;
            boolean force = AnnotationUtils.getAnnotation(targetMethod, Verify.class) != null;
            if ((globalFilter || AnnotationUtils.getAnnotation(targetMethod, UnwantedVerify.class) != null) && !force){
                if (log.isInfoEnabled()) {
                    log.info("token 验证拦截器: 请求过滤: {}, 过滤方法名:{}", request.getRequestURL(), targetMethod.getName());
                }
                return true;
            }

            String token;
            try {
                token = tokenResolver.validatorToken(tokenResolver, request);
            }catch (NoTokenException e){
                if (tokenValidatorHandler != null){
                    HttpResponseUtil.setResponseTextType(response);
                    Object result = tokenValidatorHandler.whenNoTokenHandler(e, request, response, handlerMethod);
                    HttpResponseUtil.writeResult(result, response);
                }
                return false;
            }catch (TokenExpirationException ex){
                if (tokenValidatorHandler != null){
                    HttpResponseUtil.setResponseTextType(response);
                    Object result = tokenValidatorHandler.whenTokenExpirationHandler(ex, request, response, handlerMethod);
                    HttpResponseUtil.writeResult(result, response);
                }
                return false;
            }

            //set token to request
            setToken(token, request);
        }
        return true;
    }

}

package com.black.role;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.servlet.HttpResponseUtil;
import com.black.core.servlet.TokenExpirationException;
import com.black.core.servlet.annotation.Verify;
import com.black.core.sql.code.log.Log;
import com.black.user.User;
import com.black.utils.LocalObject;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class NewestTokenIntercept implements HandlerInterceptor {

    private Configuration configuration;

    private Log log;

    private LocalObject<AntPathMatcher> pathMatcherLocal;

    public NewestTokenIntercept() {
        pathMatcherLocal = new LocalObject<>(() -> new AntPathMatcher(File.separator));
    }

    public Configuration getConfiguration() {
        if (configuration == null){
            configuration = ConfigurationHolder.getConfiguration();
            log = configuration == null ? null : configuration.getLog();
        }
        return configuration;
    }


    public boolean isOpen(){
        return getConfiguration() != null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isOpen()){
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] ==> 请求 URL 地址: " + request.getRequestURL());
            }
            //如果是一个跨域请求
            if (!CorsUtils.isPreFlightRequest(request)) {
                if (handler instanceof HandlerMethod){
                    try {
                        return doResolverToken((HandlerMethod) handler, request, response);
                    }catch (Throwable e){
                        CentralizedExceptionHandling.handlerException(e);
                        Function<Throwable, Boolean> throwableCallback = configuration.getThrowableCallback();
                        if (throwableCallback != null){
                            return throwableCallback.apply(e);
                        }
                    }
                }else if (handler instanceof ResourceHttpRequestHandler){
                    return resolveResourceRequest((ResourceHttpRequestHandler) handler, request, response);
                }
            }else {
                return dredgeCrossRequest(request, response, handler);
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (isOpen()){
            if (CorsUtils.isPreFlightRequest(request)){
                Map<String, String> responseHeadersMap = HttpResponseUtil.getResponseHeadersMap(response);
                if (log.isDebugEnabled()) {
                    log.debug("[cross] ==> 对于跨域请求结果: " + (HttpResponseUtil.CORS_FAIL == response.getStatus() ? "禁止" : "允许")
                            + ";\n响应 headersMap:" + responseHeadersMap);
                }
            }else {
                UserLocal.remove();
            }
        }
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    private boolean resolveResourceRequest(ResourceHttpRequestHandler resourceHttpRequestHandler, HttpServletRequest request, HttpServletResponse response){
        Configuration configuration = getConfiguration();
        if (configuration.isFilterResourceRequest()){
            if (log.isDebugEnabled()) {
                //放行静态资源请求
                log.debug("[token pass] ==> pass resource request");
            }
            return dredgeResourceRequest(resourceHttpRequestHandler, request, response);
        }

        if (isReourceFilterPath(request)){
            if (log.isDebugEnabled()) {
                //放行静态资源请求
                log.debug("[token pass] ==> pass resource request");
            }
            return dredgeResourceRequest(resourceHttpRequestHandler, request, response);
        }
        return false;
    }

    private boolean dredgeCrossRequest(HttpServletRequest request, HttpServletResponse response, Object handler){
        if (log.isDebugEnabled()){
            log.debug("[cross] ==> 跨域请求--发起请求域:" + request.getHeader(HttpRequestUtil.CORE_ORIGIN));
        }
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.dredgeCrossRequest(request, response, handler);
            }
        }
        return true;
    }

    private boolean doResolverToken(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response){
        final Method method = handlerMethod.getMethod();
        final Class<?> beanType = handlerMethod.getBeanType();
        final Object bean = handlerMethod.getBean();

        if (isFilterPath(request)){
            return dredgeRequest(handlerMethod, request, response);
        }

        if (!isVaildRange(method)){
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] ==> dreage request because is invaild range");
            }
            return dredgeRequest(handlerMethod, request, response);
        }

        if (isSkip(method, beanType)){
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] ==> dreage request because method is skip");
            }
            return dredgeRequest(handlerMethod, request, response);
        }

        final TokenResolver resolver = configuration.getResolver();
        String token = resolver.getToken(request, configuration);
        if (token == null){
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] <== intercept because not find token, intercept url:" + request.getRequestURL());
            }
            return resolverNoToken(request, response, method, beanType, bean);
        }

        User user;
        try {

            user = resolver.verification(configuration, token, request);
        } catch (TokenExpirationException e) {
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] <== intercept because token is already expired");
            }
            return resloverTokenExpiration(request, response, method, beanType, bean, e);
        } catch (TokenInvalidException e) {
            if (log.isDebugEnabled()) {
                log.debug("[token intercept] <== intercept because token is invaild");
            }
            return resolverTokenInvaild(request, response, method, beanType, bean, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("[token pass] ==> release request");
        }
        UserLocal.set(user);
        passVerification(handlerMethod, request, response);
        return dredgeRequest(handlerMethod, request, response);
    }

    private boolean isReourceFilterPath(HttpServletRequest request){
        String servletPath = request.getServletPath();
        Configuration configuration = getConfiguration();
        if (log.isDebugEnabled()) {
            log.debug("[token match] ==> match resource path : " + servletPath + "");
        }
        Set<String> completeMatchPath = configuration.getCompleteResourcePath();
        final Set<String> completeCopy = new HashSet<>(completeMatchPath);
        for (String cc : completeCopy) {
            if (cc.equals(servletPath)){
                if (log.isDebugEnabled()) {
                    log.debug("[token pass] ==> release resource request because match complete path: [" + cc + "]");
                }
                return true;
            }
        }

        AntPathMatcher pathMatcher = pathMatcherLocal.current();
        Set<String> matechFilterPath = configuration.getMatchResourceFilterPath();
        final Set<String> copy = new HashSet<>(matechFilterPath);
        for (String pattern : copy) {
            if (pathMatcher.match(pattern, servletPath)) {
                if (log.isDebugEnabled()) {
                    log.debug("[token pass] ==> release resource request because filter pattern: [" + pattern + "]");
                }
                return true;
            }
        }
        return false;
    }

    private boolean isFilterPath(HttpServletRequest request){
        String servletPath = request.getServletPath();
        if (log.isDebugEnabled()) {
            log.debug("[token match] ==> match path : " + servletPath + "");
        }
        Set<String> completeMatchPath = configuration.getCompleteMatchPath();
        final Set<String> completeCopy = new HashSet<>(completeMatchPath);
        for (String cc : completeCopy) {
            if (cc.equals(servletPath)){
                if (log.isDebugEnabled()) {
                    log.debug("[token pass] ==> release request because match complete path: [" + cc + "]");
                }
                return true;
            }
        }

        AntPathMatcher pathMatcher = pathMatcherLocal.current();
        Set<String> matechFilterPath = configuration.getMatechFilterPath();
        final Set<String> copy = new HashSet<>(matechFilterPath);
        for (String pattern : copy) {
            if (pathMatcher.match(pattern, servletPath)) {
                if (log.isDebugEnabled()) {
                    log.debug("[token pass] ==> release request because filter pattern: [" + pattern + "]");
                }
                return true;
            }
        }
        return false;
    }

    private void passVerification(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response){

    }

    private boolean dredgeResourceRequest(ResourceHttpRequestHandler resourceHttpRequestHandler, HttpServletRequest request, HttpServletResponse response){
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.dredgeResourceRequest(resourceHttpRequestHandler, request, response);
            }
        }
        return true;
    }

    private boolean dredgeRequest(HandlerMethod handlerMethod, HttpServletRequest request, HttpServletResponse response){
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.dredgeRequest(handlerMethod, request, response);
            }
        }
        return true;
    }

    private boolean resloverTokenExpiration(HttpServletRequest request, HttpServletResponse response,
                                            Method method, Class<?> beanType, Object bean, TokenExpirationException e){
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.resloverTokenExpiration(request, response, method, beanType, bean, e);
            }
        }
        return false;
    }

    private boolean resolverTokenInvaild(HttpServletRequest request, HttpServletResponse response,
                                         Method method, Class<?> beanType, Object bean, TokenInvalidException e){
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.resolverTokenInvaild(request, response, method, beanType, bean, e);
            }
        }
        return false;
    }

    private boolean resolverNoToken(HttpServletRequest request, HttpServletResponse response,
                                    Method method, Class<?> beanType, Object bean){
        synchronized (configuration.getListeners()){
            LinkedBlockingQueue<TokenListener> listeners = configuration.getListeners();
            for (TokenListener listener : listeners) {
                listener.resolverNoToken(request, response, method, beanType, bean);
            }
        }

        return false;
    }
    private boolean isVaildRange(Method method){
        if (configuration.isGlobalRange()){
            return true;
        }
        String name = method.getName();
        Set<String> validatorRange = configuration.getValidatorRange();
        for (String r : validatorRange) {
            if (name.startsWith(r)){
                return true;
            }
        }
        return false;
    }

    private boolean isSkip(Method method, Class<?> beanType){
        return method.isAnnotationPresent(SkipVerification.class) ||
                (beanType.isAnnotationPresent(SkipVerification.class) && !method.isAnnotationPresent(Verify.class));
    }
}

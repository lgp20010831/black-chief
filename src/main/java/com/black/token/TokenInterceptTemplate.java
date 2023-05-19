package com.black.token;

import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.core.json.JsonUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.mvc.response.Response;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.servlet.HttpResponseUtil;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.role.SkipVerification;
import com.black.role.TokenUtils;
import com.black.role.UserLocal;
import com.black.spring.ChiefSpringHodler;
import com.black.user.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作为一个 token 拦截器的模板
 * @author 李桂鹏
 * @create 2023-05-19 17:03
 */
@SuppressWarnings("all") @Getter @Setter
public abstract class TokenInterceptTemplate implements HandlerInterceptor {

    //路径匹配工具
    protected AntPathMatcher matcher;

    //标注在类或方法上的
    protected final Set<Class<? extends Annotation>> skipAnnotationType;

    //请求过滤路径
    protected final Set<String> requestFilteringPaths;

    //验证的类范围
    protected final Set<String> verifyScopes;

    //静态资源路径过滤地址
    protected final Set<String> staticResourcesAllowedReleased;

    //启用通过启动类注解
    protected boolean enabledByAnnotation = false;

    //验证范围是否为整个项目
    protected boolean verifyGlobalScope = true;

    //token 数据转换成的实体类类型
    protected Class<? extends User> userType;

    //日志
    protected IoLog log;

    //阅读token存在于header的哪一个键
    protected String agreeHeaderKey = "authorization";

    //日志前缀
    protected String defLogPrefix = "[TOKEN INTERCEPT] - ";

    //约定header值前缀
    protected String agreeValuePrefix = "Bearer ";

    //约定header值后缀
    protected String agreeValueSuffix = "";

    //启用注解类型
    protected Class<? extends Annotation> enabledByAnnotationType;

    //是否遵循静态开启
    protected volatile boolean adhereStaticOpening = true;

    //静态开启标记
    public static volatile boolean open = true;

    //是否对静态资源进行限制
    protected boolean limitStaticResourceRequest = false;

    public TokenInterceptTemplate(){
        log = LogFactory.getLog4j();
        setLogPrefix();
        skipAnnotationType = Collections.newSetFromMap(new ConcurrentHashMap<>());
        skipAnnotationType.add(SkipVerification.class);
        requestFilteringPaths = Collections.newSetFromMap(new ConcurrentHashMap<>());
        verifyScopes = Collections.newSetFromMap(new ConcurrentHashMap<>());
        staticResourcesAllowedReleased = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public void setLog(IoLog log) {
        this.log = log;
        setLogPrefix();
    }

    protected void setLogPrefix(){
        getLog().setPrefix(getDefLogPrefix());
    }

    protected AntPathMatcher createMatcher(){
        return new AntPathMatcher(File.separator);
    }

    public void addRequestFilteringPaths(String... paths){
        requestFilteringPaths.addAll(Arrays.asList(paths));
    }

    public void addSkipAnnotationTypes(Class<? extends Annotation>... classes){
        skipAnnotationType.addAll(Arrays.asList(classes));
    }

    public void addVerifyScopes(String... scopes){
        verifyScopes.addAll(Arrays.asList(scopes));
    }

    public void addStaticResourcesAllowedReleased(String... paths){
        staticResourcesAllowedReleased.addAll(Arrays.asList(paths));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!checkOpen()){
            return true;
        }
        if (log.isInfoEnabled()) {
            log.info("请求 URL 地址: {}", request.getRequestURL());
        }

        if (CorsUtils.isPreFlightRequest(request)){
            if (log.isInfoEnabled()){
                log.info("[cross] ==> 跨域请求--发起请求域: {}", request.getHeader(HttpRequestUtil.CORE_ORIGIN));
            }
            postCrossRequest(request, response, handler);
            return true;
        }

        if (handler instanceof HandlerMethod){
            return resolveHandlerMethod(request, response, (HandlerMethod) handler);
        }

        if (handler instanceof ResourceHttpRequestHandler){
            return resolveStaticRequest(request, response, (ResourceHttpRequestHandler) handler);
        }

        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!checkOpen()){
            return;
        }

        if (CorsUtils.isPreFlightRequest(request)){
            Map<String, String> responseHeadersMap = HttpResponseUtil.getResponseHeadersMap(response);
            if (log.isDebugEnabled()) {
                log.debug("[cross] ==> 对于跨域请求结果: " + (HttpResponseUtil.CORS_FAIL == response.getStatus() ? "禁止" : "允许")
                        + ";\n响应 headersMap:" + responseHeadersMap);
            }
        }else {
            UserLocal.remove();
            TokenDataLocal.remove();
        }
    }

    private boolean checkOpen(){
        if (isAdhereStaticOpening()){
            return open;
        }

        if (isEnabledByAnnotation()){
            Class<? extends Annotation> annotationType = getEnabledByAnnotationType();
            Assert.notNull(annotationType, "can not find annotationType");
            return ChiefApplicationRunner.isPertain(annotationType);
        }

        return false;
    }

    protected boolean resolveHandlerMethod(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod){
        Class<?> beanType = handlerMethod.getBeanType();
        Method method = handlerMethod.getMethod();
        Object beanDefine = handlerMethod.getBean();
        Object bean;
        DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        if (beanDefine instanceof String){
            bean = beanFactory.getBean((String) beanDefine);
        }else {
            bean = beanDefine;
        }

        if (!isScopeOfAction(beanType)){
            if (log.isDebugEnabled()) {
                log.debug("==> dreage request because is invaild range");
            }
            return true;
        }

        if (isRequestFilterPath(request)){
            postReleaseHandlerRequest(request, response, handlerMethod, method, bean);
            return true;
        }

        if (isSkipByAnnotations(beanType, method)){
            if (log.isDebugEnabled()) {
                log.debug("==> dreage request because method is skip");
            }
            postReleaseHandlerRequest(request, response, handlerMethod, method, bean);
            return true;
        }

        if (beforePerformTokenValidation(request, response, handlerMethod)){
            if (log.isDebugEnabled()) {
                log.debug("==> dreage request because user intervention");
            }
            postReleaseHandlerRequest(request, response, handlerMethod, method, bean);
            return true;
        }

        String token = getToken(request);
        if (token == null){
            if (log.isDebugEnabled()) {
                log.debug("<== intercept because not find token, intercept url:" + request.getRequestURL());
            }
            resolveNoToken(response);
            postInterceptHandlerRequest(request, response, handlerMethod, method, bean);
            return false;
        }

        if (expireToken(token)){
            if (log.isDebugEnabled()) {
                log.debug("<== intercept because token is already expired");
            }
            resolveTokenExpire(response);
            postInterceptHandlerRequest(request, response, handlerMethod, method, bean);
            return false;
        }

        if (!verifyToken(token)){
            if (log.isDebugEnabled()) {
                log.debug("<== intercept because token is invaild");
            }
            resolveTokenInvaild(response);
            postInterceptHandlerRequest(request, response, handlerMethod, method, bean);
            return false;
        }

        Object data = getDataByToken(token, request);
        if (data != null){

            JSONObject mapData = JsonUtils.letJson(data);;
            TokenDataLocal.set(new MapAttributeHandler(new JSONObject(mapData)));
            Class<? extends User> userType = getUserType();

            if (userType != null){
               data = JSONObject.toJavaObject(mapData, userType);
            }

            if (data instanceof User){
                UserLocal.set((User) data);
            }

            depositData(data);
        }

        if (log.isDebugEnabled()) {
            log.debug("==> release request");
        }
        postReleaseHandlerRequest(request, response, handlerMethod, method, bean);
        return true;
    }


    protected boolean verifyToken(String token){
        return true;
    }

    protected void depositData(Object data){

    }

    protected Object getDataByToken(String token, HttpServletRequest request){
        Map<String, Object> data = parseToken(token);
        return castTokenData(data, request);
    }

    protected Object castTokenData(Map<String, Object> data, HttpServletRequest request){
        return data;
    }

    protected Map<String, Object> parseToken(String token){
        return TokenUtils.parseToken(token);
    }

    public static boolean expireToken(String token){
        Date expireAt = TokenUtils.getExpireAt(token);
        if (expireAt != null){
            return TokenUtils.isExpire(expireAt);
        }
        return false;
    }

    protected String getToken(HttpServletRequest request){
        String header = request.getHeader(getAgreeHeaderKey());
        if (!StringUtils.hasText(header)){
            return null;

        }
        String agreeValuePrefix = getAgreeValuePrefix();
        if (StringUtils.hasText(agreeValuePrefix)){
            header = StringUtils.removeIfStartWith(header, agreeValuePrefix);
        }

        String agreeValueSuffix = getAgreeValueSuffix();
        if (StringUtils.hasText(agreeValueSuffix)){
            header = StringUtils.removeIfEndWith(header, agreeValueSuffix);
        }
        return header;
    }

    protected boolean isScopeOfAction(Class<?> beanType){
        if (isVerifyGlobalScope()){
            return true;
        }

        String typeName = beanType.getName();
        Set<String> verifyScopes = getVerifyScopes();
        for (String verifyScope : verifyScopes) {
            if (typeName.startsWith(verifyScope)){
                return true;
            }
        }
        return false;
    }

    protected boolean isSkipByAnnotations(Class<?> beanType, Method method){
        //copy
        Set<Class<? extends Annotation>> hashSet = getSkipAnnotationType();
        for (Class<? extends Annotation> type : hashSet) {
            if (AnnotationUtils.isPertain(beanType, type) || AnnotationUtils.isPertain(method, type)){
                return true;
            }
        }
        return false;
    }

    protected boolean isRequestFilterPath(HttpServletRequest request){
        String servletPath = request.getServletPath();
        if (log.isDebugEnabled()) {
            log.debug("==> match path : " + servletPath + "");
        }
        Set<String> paths = getRequestFilteringPaths();
        for (String path : paths) {
            if (matcher.match(path, servletPath)) {
                if (log.isDebugEnabled()) {
                    log.debug("==> release request because match complete path: [" + path + "]");
                }
                return true;
            }
        }
        return false;
    }

    protected boolean resolveStaticRequest(HttpServletRequest request, HttpServletResponse response, ResourceHttpRequestHandler resourceHttpRequestHandler){
        if (!isLimitStaticResourceRequest()){
            postReleaseStaticRequest(request, response, resourceHttpRequestHandler);
            return true;
        }

        if (allowResourceRelease(request, resourceHttpRequestHandler)){
            if (log.isDebugEnabled()) {
                //放行静态资源请求
                log.debug(" ==> pass resource request");
            }
            postReleaseStaticRequest(request, response, resourceHttpRequestHandler);
            return true;
        }
        postInterceptResourceRequest(request, response, resourceHttpRequestHandler);
        return false;
    }

    protected boolean allowResourceRelease(HttpServletRequest request, ResourceHttpRequestHandler resourceHttpRequestHandler){
        String servletPath = request.getServletPath();
        Set<String> released = getStaticResourcesAllowedReleased();
        for (String path : released) {
            if (matcher.match(path, servletPath)) {
                if (log.isDebugEnabled()) {
                    log.debug(" ==> release resource request because match path: [" + path + "]");
                }
                return true;
            }
        }
        return false;
    }


    protected boolean beforePerformTokenValidation(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod){
        return false;
    }


    protected void postReleaseStaticRequest(HttpServletRequest request, HttpServletResponse response, ResourceHttpRequestHandler resourceHttpRequestHandler){

    }

    protected void postReleaseHandlerRequest(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Method method, Object bean){

    }
    
    protected void postInterceptHandlerRequest(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Method method, Object bean){
        
    }

    protected void postInterceptResourceRequest(HttpServletRequest request, HttpServletResponse response, ResourceHttpRequestHandler resourceHttpRequestHandler){

    }

    protected void postCrossRequest(HttpServletRequest request, HttpServletResponse response, Object handler){

    }
    

    protected Object createNoTokenResponse(){
        return new Response(GlobalVariablePool.HTTP_CODE_NO_TOKEN, false, GlobalVariablePool.HTTP_MSG_NO_TOKEN);
    }

    protected Object createTokenExpireResponse(){
        return new Response(GlobalVariablePool.HTTP_CODE_TOKEN_EXPIRATION, false, GlobalVariablePool.HTTP_MSG_TOKEN_EXPIRATION);
    }

    protected Object createTokenInvaildResponse(){
        return new Response(GlobalVariablePool.HTTP_CODE_TOKEN_INVAILD, false, GlobalVariablePool.HTTP_MSG_TOKEN_INVAILD);
    }

    private void resolveNoToken(HttpServletResponse response){
        HttpResponseUtil.writeUtf8JsonResult(createNoTokenResponse(), response);
    }

    private void resolveTokenExpire(HttpServletResponse response){
        HttpResponseUtil.writeUtf8JsonResult(createTokenExpireResponse(), response);
    }

    private void resolveTokenInvaild(HttpServletResponse response){
        HttpResponseUtil.writeUtf8JsonResult(createTokenInvaildResponse(), response);
    }
}

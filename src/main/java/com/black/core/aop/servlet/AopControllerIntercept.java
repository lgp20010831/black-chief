package com.black.core.aop.servlet;

import com.alibaba.fastjson.JSONObject;
import com.black.GlobalVariablePool;
import com.black.Servlet;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.result.*;
import com.black.core.cache.AopControllerStaticCache;
import com.black.core.chain.*;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.JsonUtils;
import com.black.core.mvc.response.Response;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.*;
import com.black.holder.SpringHodler;
import com.black.spring.ChiefSpringHodler;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.black.core.response.Code.HANDLER_FAIL;

@Log4j2
@ChainClient
@Adaptation(GetAopInterceptAdapter.class) @SuppressWarnings("all")
public class AopControllerIntercept implements AopTaskIntercepet, CollectedCilent {

    public static final String TASK_ALIAS = "http-around";

    public static final ThreadLocal<Throwable> voidResponseThrowLocal = new ThreadLocal<>();

    public static final ThreadLocal<BeforeWriteSession> writeResponseSessionLocal = new ThreadLocal<>();

    public static final ThreadLocal<Class<?>> controllerTypeLocal = new ThreadLocal<>();

    public static Class<? extends Annotation> startPageClazz = OpenIbatisPage.class;

    public static final ThreadLocal<Long> lazyPageEnhanceLocal = new ThreadLocal<>();

    public static String pageSizeName = "pageSize";

    public static String pageNumName = "pageNum";

    private boolean defaultHandlerError = true;

    private final ThreadLocal<Page<Object>> pageThreadLocal = new ThreadLocal<>();

    /***
     * 对拦截方法的封装, 封装成 RestWrapper
     */
    private final Map<GroupKeys, HttpMethodWrapper> wrapperMap = new ConcurrentHashMap<>();

    //order 越小, 越先执行, 越接近最原始的参数和结果
    private Collection<Object> resolvers;

    public AopControllerIntercept(){
        AopControllerStaticCache.setControllerIntercept(this);
    }

    //入口
    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Method method = hijack.getMethod();
        Class<?> hijackClazz = hijack.getClazz();
        Object[] args = hijack.getArgs();
        Object aThis = hijack.getInvocation().getThis();
        //获取解析的wrapper
        HttpMethodWrapper httpMethodWrapper = processorMethodBeforeEnhance(method, hijackClazz, args);

        //如果需要打印
        if (isPrint(hijackClazz)){
            printLog(httpMethodWrapper);
        }

        try {
            saveHttpMethodWrapper(httpMethodWrapper);
            processServletArg(args, httpMethodWrapper, aThis);
            boolean startPage = false;
            if (httpMethodWrapper.isPage()) {
                startPage = startPage(httpMethodWrapper);
            }

            Object result = handlerInvoke(method, hijackClazz, hijack, httpMethodWrapper);
            if (startPage){
                return pageResult(result);
            }
            return result;
        }finally {
            pageThreadLocal.remove();
            closeHttpManager();
            endServlet(aThis);
        }
    }

    protected void endServlet(Object instance){
        if(instance instanceof Servlet){
            ((Servlet) instance).fetchFinishCallback();
        }
    }

    protected void processServletArg(Object[] args, HttpMethodWrapper httpMethodWrapper, Object instance){
        if(!(instance instanceof Servlet)){
            return;
        }
        ((Servlet) instance).setArg(args);
        AopChiefServletConfiguration configuration = AopChiefServletConfiguration.getInstance();
        handlerRequestBody(args, httpMethodWrapper.getHttpMethod(), (Servlet) instance);
        handlerRequestPart(args, httpMethodWrapper.getHttpMethod(), (Servlet) instance);
    }

    protected void handlerRequestBody(Object[] args, Method method, Servlet servlet){
        MethodWrapper methodWrapper = MethodWrapper.get(method);
        ParameterWrapper bodyPw = methodWrapper.getSingleParameterByAnnotation(RequestBody.class);
        if (bodyPw != null){
            Object body = args[bodyPw.getIndex()];
            servlet.setBody(body);
        }
    }

    protected void handlerRequestPart(Object[] args, Method method, Servlet servlet){
        MethodWrapper methodWrapper = MethodWrapper.get(method);
        List<ParameterWrapper> parts = methodWrapper.getParameterByAnnotation(RequestPart.class);
        if (!Utils.isEmpty(parts)){
            Map<String, Object> partMap = new LinkedHashMap<>();
            for (ParameterWrapper part : parts) {
                RequestPart annotation = part.getAnnotation(RequestPart.class);
                String value = annotation.value();
                partMap.put(value, args[part.getIndex()]);
            }
            servlet.setPart(partMap);
        }
    }


    //****************************************
    //                  具体处理
    //****************************************
    protected Object handlerInvoke(Method method, Class<?> clazz, HijackObject hijack, HttpMethodWrapper httpMethodWrapper) throws Throwable {
        Class<?> returnType = method.getReturnType();
        MethodWrapper methodWrapper = httpMethodWrapper.getMethodWrapper();
        ClassWrapper<?> classWrapper = ClassWrapper.get(clazz);
        Class<? extends RestResponse> responseClass = getResponseClassType(clazz);
        boolean enhanceResult = isNeedenhanceResult(returnType, method, clazz);
        Object result = null;
        RestResponse response = null;
        Object[] args = hijack.getArgs();
        try {
            long prepareStartTime = System.currentTimeMillis();
            //处理参数
            args = handlerArgs(args, httpMethodWrapper);

            //判断方法是否被拦截
            if (!intercept(args, httpMethodWrapper)){
                //没有拦截, 执行前置处理
                args = beforeInvoke(args, httpMethodWrapper);
                if (log.isInfoEnabled()) {
                    log.info("prepare time: {} ms", (System.currentTimeMillis() - prepareStartTime));
                }
                long startTime = System.currentTimeMillis();
                //处理实际业务
                Object invokeResult = hijack.doRelease(args);

                //执行后置处理
                result = handlerAfterInvoker(invokeResult, httpMethodWrapper, responseClass);
                if (log.isInfoEnabled()) {
                    log.info("程序执行时间: {} 毫秒", (System.currentTimeMillis() - startTime));
                }
            }else {
                if (log.isInfoEnabled()) {
                    log.info("intercept controller invoke ...");
                }
                //拦截成功, 执行回调方法
                result = interceptCallBack(hijack, args, httpMethodWrapper);
            }
        } catch (Throwable e) {

            //当捕获到异常时
            /* logo for error */
            CentralizedExceptionHandling.handlerException(e);
            if (isForeThrowsThrowable(methodWrapper, classWrapper)){
                throw e;
            }

            if (enhanceResult) {
                //遍历处理器试图处理异常
                //user handler error
                result = handlerException(e, responseClass, httpMethodWrapper);
            } else {
                if (hasResponseVoidWritor()){
                    voidResponseThrowLocal.set(e);
                }else {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            //处理响应类
            if (result instanceof RestResponse){
                response = (RestResponse) result;
            }else if (response == null && enhanceResult) {
                //创建响应类
                response = createRestResponse(responseClass, GlobalVariablePool.HTTP_CODE_SUCCESSFUL,
                        true, GlobalVariablePool.HTTP_MSG_SUCCESSFUL, result, httpMethodWrapper);
            }
        }
        Object servletResponse = enhanceResult ? response : result;
        if (hasResponseVoidWritor()){
            controllerTypeLocal.set(clazz);
            resolveWriteResponse(methodWrapper, classWrapper, args);
        }
        return servletResponse;
    }

    private void saveHttpMethodWrapper(HttpMethodWrapper httpMethodWrapper){
        HttpMethodManager.save(httpMethodWrapper);
    }

    private void closeHttpManager(){
        HttpMethodManager.close();
    }

    private void resolveWriteResponse(MethodWrapper methodWrapper, ClassWrapper<?> classWrapper, Object[] args){
        ChiefResponseAdvice annotation = methodWrapper.getAnnotation(ChiefResponseAdvice.class);
        if (annotation == null){
            annotation = classWrapper.getAnnotation(ChiefResponseAdvice.class);
        }
        if (annotation == null){
            return;
        }
        com.black.core.factory.beans.BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        Class<? extends ChiefBeforeWriteResolver>[] classes = annotation.value();
        List<ChiefBeforeWriteResolver> resolvers = new ArrayList<>();
        for (Class<? extends ChiefBeforeWriteResolver> type : classes) {
            try {
                resolvers.add(beanFactory.getSingleBean(type));
            }catch (Throwable e){
                log.warn("instance response resolver fair: {}", e.getMessage());
            }
        }
        BeforeWriteSession session = new BeforeWriteSession(args, methodWrapper, classWrapper, resolvers);
        writeResponseSessionLocal.set(session);
    }



    private boolean isForeThrowsThrowable(MethodWrapper mw, ClassWrapper<?> cw){
        return mw.hasAnnotation(ThrowsThrowable.class) || cw.hasAnnotation(ThrowsThrowable.class);
    }

    protected Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper){
        Object[] result = args;
        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                result = aroundResolver.handlerArgs(result, httpMethodWrapper);
            }
        }
        return result;
    }

    protected Object[] beforeInvoke(Object[] args, HttpMethodWrapper httpMethodWrapper){
        Object[] result = args;
        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                result = aroundResolver.beforeInvoke(result, httpMethodWrapper);
            }
        }
        return result;
    }

    protected Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass){
        Object handlerResult = result;
        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                handlerResult = aroundResolver.handlerAfterInvoker(handlerResult, httpMethodWrapper, responseClass);
            }
        }
        return handlerResult;
    }

    protected Object handlerException(Throwable e, Class<? extends RestResponse> responseClass, HttpMethodWrapper httpMethodWrapper) throws Throwable{

        Object result = null;
        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                try {
                    result = aroundResolver.handlerException(e, responseClass, httpMethodWrapper);
                }catch (Throwable ex){
                    e = ex;
                }
            }
        }
        if (result == null){
            if (defaultHandlerError){
                /* matching error */
                return createRestResponse(responseClass, HANDLER_FAIL.value(),
                        false, e instanceof RuntimeException ? e.getMessage() : GlobalVariablePool.HTTP_MSG_FAIL, e, httpMethodWrapper);
            }
        }
        return result;
    }

    protected boolean intercept(Object[] args, HttpMethodWrapper httpMethodWrapper){

        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                if (aroundResolver.intercept(args, httpMethodWrapper)) {
                    return true;
                }
            }
        }
        return false;
    }


    protected Object interceptCallBack(HijackObject hijack, Object[] args, HttpMethodWrapper httpMethodWrapper){
        Object chainResult = null;
        if (resolvers != null){
            for (Object resolver : resolvers) {
                GlobalAroundResolver aroundResolver = (GlobalAroundResolver) resolver;
                chainResult = aroundResolver.interceptCallBack(hijack, args, httpMethodWrapper, chainResult);
            }
        }
        return chainResult;
    }

    public DefaultListableBeanFactory getBeanFactory(){
        DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        if (beanFactory == null){
            beanFactory = SpringHodler.getListableBeanFactory();
        }
        return beanFactory;
    }

    private Boolean hasResponseVoidWritor;
    private boolean hasResponseVoidWritor(){
        if (hasResponseVoidWritor == null){
            try {
                getBeanFactory().getBean(ResponseVoidWritor.class);
                hasResponseVoidWritor = true;
            }catch (Throwable e){
                hasResponseVoidWritor = false;
            }
        }
        return hasResponseVoidWritor;
    }

    public void setDefaultHandlerError(boolean defaultHandlerError) {
        this.defaultHandlerError = defaultHandlerError;
    }

    //********************************************
    //              创建响应类策略
    //********************************************
    public static RestResponse createRestResponse(Class<? extends RestResponse> target, int dcode, boolean ds,
                                                  String dmessage, Throwable e, HttpMethodWrapper mw){
        return createRestResponse(target, dcode, ds, dmessage, null, false, e, mw);
    }

    public static RestResponse createRestResponse(Class<? extends RestResponse> target,
                                                  int dCode, boolean ds, String dMessage, Object result, HttpMethodWrapper mw){
        return createRestResponse(target, dCode, ds, dMessage, result, true, null, mw);
    }

    //d = default
    public static RestResponse createRestResponse(Class<? extends RestResponse> target,
                                              int dCode, boolean ds, String dMessage, HttpMethodWrapper mw){
        return createRestResponse(target, dCode, ds, dMessage, null, true, null, mw);
    }

    public static RestResponse createRestResponse(Class<? extends RestResponse> target,
                                              int dCode, boolean ds, String dMessage, Object result,
                                                  boolean stateNormal, Throwable ex, HttpMethodWrapper mw){
        RestResponse restResponse;
        try {
             restResponse = target.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            CentralizedExceptionHandling.handlerException(e);
            throw new RuntimeException("响应类需要存在无参构造器", e);
        }
        boolean fill = false;
        AopControllerIntercept intercept = AopControllerStaticCache.getControllerIntercept();
        if (intercept != null && mw != null && intercept.resolvers != null){
            for (Object resolver : intercept.resolvers) {
                GlobalAroundResolver ar = (GlobalAroundResolver) resolver;
                if (stateNormal){
                    if (ar.createResponseByRegular(restResponse, result, mw)) {
                        fill = true;
                        break;
                    }
                }else if (!stateNormal && ex != null){
                    if (ar.createResponseByError(restResponse, ex, mw)) {
                        fill = true;
                        break;
                    }
                }
            }
        }
        if (!fill){
            restResponse.setCode(dCode);
            restResponse.setSuccessful(ds);
            restResponse.setResult(result);
            restResponse.setMessage(dMessage);
            if(!stateNormal && ex != null){
                if (restResponse.enabledThrowableStack()) {
                    restResponse.setThrowableStackTrace(ExceptionUtil.getStackTraceInfo(ex));
                }
            }
        }
        return restResponse;
    }

    public static Class<? extends RestResponse> getResponseClassType(Class<?> clazz){
        AopChiefServletConfiguration configuration = AopChiefServletConfiguration.getInstance();
        if (configuration.isUseGlobalResponse()) {
            return configuration.getGlobalResponseType();
        }
        Class<? extends RestResponse> type = RestResponseLocal.getType();
        if (type != null){
            return type;
        }
        GlobalEnhanceRestController controller = AnnotatedElementUtils.findMergedAnnotation(clazz, GlobalEnhanceRestController.class);
        if (controller != null){
            return controller.value();
        }else {
            return configuration.getDefaultResponseType();
        }
    }

    public static boolean isNeedenhanceResult(Class<?> returnType, Method method, Class<?> clazz){

        return (Response.class.isAssignableFrom(returnType) || Object.class.equals(returnType))
                && !void.class.equals(returnType)
                && AnnotationUtils.getAnnotation(method, UnEnhancementRequired.class) == null
                && AnnotationUtils.getAnnotation(clazz, UnEnhancementRequired.class) == null;
    }


    private Object pageResult(Object responseResult) {
        if (responseResult instanceof RestResponse) {
            RestResponse response = (RestResponse) responseResult;
            List<Object> result = (List<Object>) response.obtainResult();
            if (result != null){
                PageInfo<Object> info = new PageInfo<>(result);
                Long total = response.obtainTotal();
                if (total == null){
                    response.setTotal(info.getTotal());
                }

            }else {
                Long total = response.obtainTotal();
                if (total == null){
                    response.setTotal(0L);
                }
            }
            return response;
        }else {
            List<Object> list = SQLUtils.wrapList(responseResult);
            PageInfo<Object> info = new PageInfo<>(list);
            lazyPageEnhanceLocal.set(info.getTotal());
            return responseResult;
        }
    }

    protected boolean isPrint(Class<?> clazz){
        AopChiefServletConfiguration configuration = AopChiefServletConfiguration.getInstance();
        if (configuration.isUseGlobalPrintLog()) {
            return configuration.isGlobalPrintLog();
        }
        GlobalEnhanceRestController controller = AnnotationUtils.getAnnotation(clazz, GlobalEnhanceRestController.class);
        return controller != null && controller.printLog();
    }

    public boolean startPage(HttpMethodWrapper restWrapper) {
        Integer pageSize = null, pageNum = null;
        if (restWrapper.isJsonRequest()) {
            Parameter[] parameters;
            for (int i = 0; i < (parameters = restWrapper.getHttpMethod().getParameters()).length; i++) {
                if (AnnotationUtils.getAnnotation(parameters[i], RequestBody.class) != null) {
                    try {
                        Object wrapperArg = restWrapper.getArgs()[i];
                        if (wrapperArg == null) {
                            return false;
                        }
                        JSONObject body;
                        if (wrapperArg instanceof JSONObject) {
                            body = (JSONObject) wrapperArg;
                        } else {
                            body = JsonUtils.letJson(wrapperArg);
                        }
                        pageNum = body.getInteger(restWrapper.getPageNumArgName());
                        pageSize = body.getInteger(restWrapper.getPageSizeArgName());
                    } catch (Throwable e) {
                        if (log.isErrorEnabled()) {
                            log.error("尝试获取分页信息失败, 无法解析json参数");
                        }
                        CentralizedExceptionHandling.handlerException(e);
                    }
                }
            }
        }

        if (pageSize == null || pageNum == null){
            pageSize = Convert.toInt(getRequest().getParameter(restWrapper.getPageSizeArgName()));
            pageNum = Convert.toInt(getRequest().getParameter(restWrapper.getPageNumArgName()));
        }

        if (pageSize == null || pageNum == null) {
            return false;
        }
        doStartPage(pageSize, pageNum);
        return true;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private void doStartPage(Integer pageSize, Integer pageNum) {
        Page<Object> objectPage = PageHelper.startPage(pageNum, pageSize);
        pageThreadLocal.set(objectPage);
    }

    public ThreadLocal<Page<Object>> getPageThreadLocal() {
        return pageThreadLocal;
    }

    protected HttpMethodWrapper processorMethodBeforeEnhance(Method method, Class<?> clazz, Object[] args){
        GroupKeys groupKeys = new GroupKeys(method, clazz);
        HttpMethodWrapper restWrapper = wrapperMap.get(groupKeys);
        if (restWrapper != null){
            restWrapper.setArgs(args);
            return restWrapper;
        }
        try {
            String[] path = null;
            RequestMapping resultMapping = AnnotationUtils.getAnnotation(clazz, RequestMapping.class);
            if (resultMapping != null) {
                path = resultMapping.value();
            }
            PostMapping postMapping;
            DeleteMapping deleteMapping;
            PutMapping putMapping;
            GetMapping getMapping;
            String[] methodPath = new String[0];
            if ((postMapping = AnnotationUtils.getAnnotation(method, PostMapping.class)) != null) {
                methodPath = postMapping.value();
            } else if ((deleteMapping = AnnotationUtils.getAnnotation(method, DeleteMapping.class)) != null) {
                methodPath = deleteMapping.value();
            } else if ((putMapping = AnnotationUtils.getAnnotation(method, PutMapping.class)) != null) {
                methodPath = putMapping.value();
            } else if ((getMapping = AnnotationUtils.getAnnotation(method, GetMapping.class)) != null) {
                methodPath = getMapping.value();
            } else if ((resultMapping = AnnotationUtils.getAnnotation(method, RequestMapping.class)) != null) {
                methodPath = resultMapping.value();
            }
            ArrayList<String> splicingPath = new ArrayList<>();
            for (String s : methodPath) {
                String mp = s.startsWith("/") ? s : ("/".concat(s));
                if (path != null && path.length != 0) {
                    for (String p : path) {
                        splicingPath.add(StringUtils.linkStr(p.startsWith("/") ? p : ("/".concat(p)), mp));
                    }
                } else {
                    splicingPath.add(mp);
                }
            }

            restWrapper = new HttpMethodWrapper(splicingPath, getRequest().getContentType(), clazz, method, args);
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error("解析控制器方法: {}, 发生异常", method);
            }
            CentralizedExceptionHandling.handlerException(e);
        } finally {
            if (restWrapper != null) {
                wrapperMap.put(groupKeys, restWrapper);
            }
        }
        return restWrapper;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin();
        entry.needOrder(true);
        entry.setAlias(TASK_ALIAS);
        entry.condition(c ->{
            return GlobalAroundResolver.class.isAssignableFrom(c) &&
                    AnnotationUtils.getAnnotation(c, GlobalAround.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (TASK_ALIAS.equals(resultBody.getAlias())) {
            resolvers = resultBody.getCollectSource();
        }
    }

    public static void printLog(HttpMethodWrapper restWrapper) {
        HttpServletRequest request = getRequest();
        try {
            if (log.isInfoEnabled()) {
                if (GlobalServlet.eyeCatchingLog){
                    Cookie[] cookies = request.getCookies();
                    StringJoiner cookiesJoiner = new StringJoiner(",");
                    if (cookies != null){
                        for (int i = 0; i < cookies.length; i++) {
                            Cookie cookie = cookies[i];
                            if (cookie != null){
                                cookiesJoiner.add(cookie.getName() + " = " + cookie.getValue());
                            }else {
                                cookiesJoiner.add("null");
                            }
                        }
                    }
                    log.info(AnsiOutput.toString(AnsiColor.BRIGHT_RED, "\n 接口调用 ===> 控制器: " ,
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 方法名: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求路径: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求方法: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求URL地址: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " Cookies: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 客户端地址: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 参数列表: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " Content-Type: ",
                                    AnsiColor.BLUE, "{};\n"),
                            restWrapper.getControllerClazz().getSimpleName(), restWrapper.getHttpMethod().getName(),
                            restWrapper.showPath(), request.getMethod(), request.getRequestURL(),
                            cookiesJoiner.toString(), HttpRequestUtil.getIpAddr(request),
                            restWrapper.showArgs(), restWrapper.getContentType());
                }else {
                    log.info("接口调用 ===> 控制器: {};\n 方法名:{};\n 请求路径:{};\n 参数列表:{};\n Content-Type: {}",
                            restWrapper.getControllerClazz().getSimpleName(), restWrapper.getHttpMethod().getName(),
                            restWrapper.showPath(), restWrapper.showArgs(), restWrapper.getContentType());
                }
            }
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error("打印参数时发生异常");
            }
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}

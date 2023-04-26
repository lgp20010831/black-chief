package com.black.core.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.black.core.mvc.response.Response;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.servlet.annotation.Token;
import com.black.core.util.Convert;
import com.black.core.util.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.black.core.response.Code.*;
import static com.black.core.response.VariablePool.WORK_FAIL;
import static com.black.core.response.VariablePool.WORK_SUCCESSFUL;

@Log4j2
public abstract class AbstractAopTemplate {

    /**
     * http 请求单例
     */
    protected final HttpServletRequest request;


    /** spring bean 工厂 */
    private BeanFactory beanFactory;

    /** 判断方法是否分页, 需要在方法上添加该注解 */
    public static Class<? extends Annotation> startPageClazz = StartPage.class;

    /***
     *  判断代理的方法是否为控制器方法， 是否需要被拦截
     */
    private final Map<Method, Boolean> mangentMethods = new ConcurrentHashMap<>();

    /***
     * 解析方法获取方法上标注的所有注解,并存入缓存
     */
    private final Map<Method, Collection<Annotation>> annotaionCache = new ConcurrentHashMap<>();

    /***
     * 对拦截方法的封装, 封装成 RestWrapper
     */
    private final Map<Method, RestWrapper> wrapperMap = new ConcurrentHashMap<>();

    public AbstractAopTemplate(HttpServletRequest request) {
        this.request = request;
    }

    protected Object enhanceByTemplate(ProceedingJoinPoint point) throws Throwable{
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method signatureMethod = signature.getMethod();
        parseSignature(point, signature, signatureMethod);

        Boolean handler;
        if ((handler = mangentMethods.get(signatureMethod)) == null || !handler) {
            //不再监控范围内
            return handlerNonInterception(point, signatureMethod);
        }

        RestWrapper restWrapper = wrapperMap.get(signatureMethod);
        compareWrapper(restWrapper, point);
        //打印调用接口的日志
        printLog(restWrapper);
        boolean startPage = false;
        if (restWrapper.isPage(annotaionCache)) {
            startPage = startPage(restWrapper);
        }
        Object response = handlerInvoke(point, signatureMethod.getReturnType(), signatureMethod);
        if (startPage) {
            return pageResult(response);
        }
        return response;
    }


    private void compareWrapper(RestWrapper wrapper, ProceedingJoinPoint point) {
        String type = wrapper.getContentType();
        if (type != null) {
            if (!type.equals(request.getContentType())) {
                wrapper.setContentType(request.getContentType());
            }
        }
        wrapper.setArgs(point.getArgs());
    }

    protected Object handlerNonInterception(ProceedingJoinPoint point, Method method) throws Throwable {
        return point.proceed(point.getArgs());
    }

    /***
     * 返回处理前的参数, 对参数进行处理
     * @param point point
     * @param method 代理的方法
     * @param args 目前参数
     * @return 返回结果
     */
    protected abstract Object[] beforeInvoke(ProceedingJoinPoint point, Method method, Object[] args);

    /***
     * 处理异常, 如果在你可以处理的范围内, 则定义返回值
     * 如果返回空,则会走默认的处理机制
     * @param e 异常
     * @param point point
     * @return 处理结果
     */
    protected abstract Object handlerException(Throwable e, ProceedingJoinPoint point, Method method);

    /***
     * 当处理完成以后, 在对结果进一步处理
     * @param result 处理之后的结果
     * @param point point
     * @return 返回的结果就是最终结果
     */
    protected abstract Object handlerAfterInvoker(Object result, ProceedingJoinPoint point, Method method);

    private Object handlerInvoke(ProceedingJoinPoint point, Class<?> returnType, Method method) {
        boolean enhanceResult = isNeedenhanceResult(returnType, method);
        Object result = null;
        Response response = null;
        Object[] args = handlerArgs(point.getArgs(), method);
        try {
            args = beforeInvoke(point, method, args);
            long startTime = System.currentTimeMillis();
            result = handlerAfterInvoker(point.proceed(args), point, method);
            if (log.isInfoEnabled()) {
                log.info("程序执行时间: {} 毫秒", (System.currentTimeMillis() - startTime));
            }
        } catch (Throwable e) {

            /* logo for error */
            CentralizedExceptionHandling.handlerException(e);
            if (enhanceResult) {

                //user handler error
                result = handlerException(e, point, method);
                if (result == null){
                    /* matching error */
                    if (e instanceof RuntimeException) {
                        response = new Response(HANDLER_FAIL.value(), false, e.getMessage());
                    } else if (e instanceof FileSizeLimitExceededException) {
                        response = new Response(UPLOAD_FILE_FAIL.value(), false, "文件上传最大不能超过10MB");
                    } else {
                        /* 构造 result */
                        response = new Response(HANDLER_FAIL.value(), false, WORK_FAIL);
                    }
                }
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            if (result instanceof Response){
                response = (Response) result;
            }else if (response == null && enhanceResult) {
                response = new Response(SUCCESS.value(), true, WORK_SUCCESSFUL, result);
            }
        }
        return enhanceResult ? response : result;
    }


    protected boolean isNeedenhanceResult(Class<?> returnType, Method method){
        return (Response.class.isAssignableFrom(returnType) || Object.class.equals(returnType))
                && AnnotationUtils.getAnnotation(method, UnEnhanceResult.class) == null;
    }

    protected Object[] handlerArgs(Object[] args, Method method){
        Parameter[] parameters = method.getParameters();
        if (args.length != parameters.length){
            if (log.isErrorEnabled()) {
                log.error("aop 参数映射下标无法对应: args.index: {}, parameters.index:{}",
                        args.length, parameters.length);
            }
            return args;
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (AnnotationUtils.getAnnotation(parameter, Token.class) != null){
                args[i] = getToken();
                break;
            }
        }
        return args;
    }

    protected String getToken(){
        Object attribute = request.getAttribute(HttpRequestUtil.REQUEST_TOKEN_PARAM);
        return attribute == null ? null : attribute.toString();
    }

    public boolean startPage(RestWrapper restWrapper) {
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
                            body = JSON.parseObject(wrapperArg.toString());
                        }
                        pageNum = body.getInteger(getRequestPageNumParam());
                        pageSize = body.getInteger(getRequestPageSizeParam());
                    } catch (Throwable e) {
                        if (log.isErrorEnabled()) {
                            log.error("尝试获取分页信息失败, 无法解析json参数");
                        }
                        CentralizedExceptionHandling.handlerException(e);
                    }
                }
            }
        } else {
            pageSize = Convert.toInt(request.getParameter(getRequestPageSizeParam()));
            pageNum = Convert.toInt(request.getParameter(getRequestPageNumParam()));
        }
        if (pageSize == null || pageNum == null) {
            return false;
        }
        doStartPage(pageSize, pageNum);
        return true;
    }


    private void doStartPage(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
    }

    private Object pageResult(Object responseResult) {
        if (responseResult instanceof Response) {
            Response response = (Response) responseResult;
            List<Object> result = (List<Object>) response.getResult();
            if (result != null){
                PageInfo<Object> info = new PageInfo<>(result);
                response.setTotal(info.getTotal());
            }else {
                response.setTotal(0L);
            }
            return response;
        }
        throw new RuntimeException("responseResult 不是 response 类型");
    }

    private String getRequestPageSizeParam() {
        return "pageSize";
    }

    private String getRequestPageNumParam() {
        return "pageNum";
    }

    //解析代理方法，加载到缓存中
    private void parseSignature(ProceedingJoinPoint point, MethodSignature signature, Method signatureMethod) {
        if (!mangentMethods.containsKey(signatureMethod)) {
            mangentMethods.put(signatureMethod, parseMethod(signatureMethod, point.getTarget(),
                    signature.getDeclaringType(), point.getArgs()));
        }

        if (!annotaionCache.containsKey(signatureMethod)) {
            annotaionCache.put(signatureMethod, Arrays.asList(signatureMethod.getAnnotations()));
        }
    }

    private boolean parseMethod(Method method, Object target, Class<?> targetClazz, Object[] args) {
        if (AnnotationUtils.getAnnotation(targetClazz, Controller.class) == null) {
            return false;
        }
        RestWrapper restWrapper = null;
        try {
            String[] path = null;
            RequestMapping resultMapping = AnnotationUtils.getAnnotation(targetClazz, RequestMapping.class);
            if (resultMapping != null) {
                path = resultMapping.value();
            }
            PostMapping postMapping;
            DeleteMapping deleteMapping;
            PutMapping putMapping;
            GetMapping getMapping;
            String[] methodPath;
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
            } else {
                return false;
            }
            ArrayList<String> splicingPath = new ArrayList<>();
            for (int i = 0; i < methodPath.length; i++) {
                String mp = methodPath[i].startsWith("/") ? methodPath[i] : ("/".concat(methodPath[i]));
                if (path != null && path.length != 0) {
                    for (int j = 0; j < path.length; j++) {
                        String p = path[j];
                        splicingPath.add(StringUtils.linkStr(p.startsWith("/") ? p : ("/".concat(p)), mp));
                    }
                } else {
                    splicingPath.add(mp);
                }
            }
            restWrapper = new RestWrapper(splicingPath, request.getContentType(), targetClazz, method, args);
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error("解析控制器方法: {}, 发生异常", method);
            }
            CentralizedExceptionHandling.handlerException(e);
        } finally {
            if (restWrapper != null) {
                wrapperMap.put(method, restWrapper);
            }
        }

        return true;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Getter
    @Setter
    public static class RestWrapper {
        Collection<String> requestPaths;
        //text/plain;charset=UTF-8
        String contentType;
        Class<?> controllerClazz;
        Method httpMethod;
        Object[] args;
        Boolean page;
        Boolean hasRequestBody;

        public RestWrapper(Collection<String> requestPaths, String contentType, Class<?> controllerClazz, Method httpMethod, Object[] args) {
            this.requestPaths = requestPaths;
            this.contentType = contentType;
            this.controllerClazz = controllerClazz;
            this.httpMethod = httpMethod;
            this.args = args;
        }

        public boolean isJsonRequest() {
            return contentType == null ? hasRequestBody() :
                    (contentType.startsWith("application/json") || contentType.startsWith("text/plain"));
        }

        private boolean hasRequestBody(){
            if (hasRequestBody == null){
                for (Parameter parameter : httpMethod.getParameters()) {
                    if (AnnotationUtils.getAnnotation(parameter, RequestBody.class) != null){
                        return hasRequestBody = true;
                    }
                }
                return hasRequestBody = false;
            }
            return hasRequestBody;
        }

        public String  showArgs() {
            if (args == null || args.length == 0) {
                return "[]";
            }
            return Arrays.asList(args).toString();
        }

        public String showPath() {
            return Arrays.asList(requestPaths).toString();
        }

        public boolean isPage(Map<Method, Collection<Annotation>> annotaionCache) {
            if (page == null) {
                if (annotaionCache.containsKey(httpMethod)) {
                    for (Annotation annotation : annotaionCache.get(httpMethod)) {
                        if (startPageClazz.isAssignableFrom(annotation.getClass())) {
                            page = true;
                            break;
                        }
                    }
                    if (page == null) {
                        page = false;
                    }
                }
            }
            return Boolean.TRUE.equals(page);
        }
    }

    public static void printLog(RestWrapper restWrapper) {
        try {
            if (log.isInfoEnabled()) {
                log.info("接口调用 ===> 控制器: {};\n 方法名:{};\n 请求路径:{};\n 参数列表:{};\n Content-Type: {}",
                        restWrapper.controllerClazz.getSimpleName(), restWrapper.getHttpMethod().getName(),
                        restWrapper.showPath(), restWrapper.showArgs(), restWrapper.getContentType());
            }
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error("打印参数时发生异常");
            }
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}

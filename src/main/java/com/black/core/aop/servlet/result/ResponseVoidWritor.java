package com.black.core.aop.servlet.result;

import com.black.GlobalVariablePool;
import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.aop.servlet.RestResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.List;

import static com.black.core.response.Code.HANDLER_FAIL;

@Log4j2 @Order(10000)
@RestControllerAdvice
public class ResponseVoidWritor implements ResponseBodyAdvice {


    /**
     * Whether this component supports the given controller method return type
     * and the selected {@code HttpMessageConverter} type.
     *
     * @param returnType    the return type
     * @param converterType the selected converter type
     * @return {@code true} if {@link #beforeBodyWrite} should be invoked;
     * {@code false} otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        //获取方法的所在类
        Class<?> declaringClass = returnType.getDeclaringClass();
        Method method = returnType.getMethod();
        Class<?> methodReturnType = method.getReturnType();
        GlobalEnhanceRestController annotation = AnnotationUtils.getAnnotation(declaringClass, GlobalEnhanceRestController.class);
        //判断这个类有没有标注这个特殊的注解
        return annotation != null  &&
                AnnotationUtils.getAnnotation(method, UnEnhancementResponse.class) == null &&
                AnnotationUtils.getAnnotation(declaringClass, UnEnhancementResponse.class) == null;
    }

    //@ExceptionHandler(Throwable.class)
    public Object handlerThrowable(Throwable throwable){
        return null;
    }


    /**
     * Invoked after an {@code HttpMessageConverter} is selected and just before
     * its write method is invoked.
     *
     * @param body                  the body to be written
     * @param returnType            the return type of the controller method
     * @param selectedContentType   the content type selected through content negotiation
     * @param selectedConverterType the converter type selected to write to the response
     * @param request               the current request
     * @param response              the current response
     * @return the body that was passed in or a modified (possibly new) instance
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String subtype = selectedContentType.getSubtype();
        if (!subtype.contains("json") && !subtype.contains("plain")){
            if (log.isDebugEnabled()) {
                log.debug("write void response fail, because response Content-type is not json");
            }
            return body;
        }
        Class<?> declaringClass = returnType.getDeclaringClass();
        Class<? extends RestResponse> responseClassType = AopControllerIntercept.getResponseClassType(declaringClass);
        final ThreadLocal<Throwable> throwableThreadLocal = AopControllerIntercept.voidResponseThrowLocal;
        Throwable throwable = throwableThreadLocal.get();
        RestResponse restResponse;
        if (body instanceof RestResponse){
            restResponse = (RestResponse) body;
        }
        else {
            if (throwable == null){
                restResponse = AopControllerIntercept.createRestResponse(responseClassType, GlobalVariablePool.HTTP_CODE_SUCCESSFUL,
                        true, GlobalVariablePool.HTTP_MSG_SUCCESSFUL, body, null);
            }else {
                restResponse = AopControllerIntercept.createRestResponse(responseClassType, HANDLER_FAIL.value(),
                        false, throwable instanceof RuntimeException ? throwable.getMessage() : GlobalVariablePool.HTTP_MSG_FAIL, throwable, null);
                throwableThreadLocal.remove();
            }
        }

        ThreadLocal<BeforeWriteSession> responseSessionLocal = AopControllerIntercept.writeResponseSessionLocal;
        BeforeWriteSession session = responseSessionLocal.get();
        if (session != null){
            List<ChiefBeforeWriteResolver> resolvers = session.getResolvers();
            try {
                for (ChiefBeforeWriteResolver resolver : resolvers) {
                    try {
                        restResponse = resolver.resolver(restResponse, session.getArgs(), session.getMw(), session.getCw());
                    }catch (Throwable e){
                        log.warn("ChiefBeforeWriteResolver resolve fair: {}", e.getMessage());
                    }
                }
            }finally {
                responseSessionLocal.remove();
            }
        }
        return restResponse;
    }
}

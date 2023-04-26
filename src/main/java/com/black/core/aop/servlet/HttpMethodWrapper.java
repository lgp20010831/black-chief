package com.black.core.aop.servlet;

import com.black.core.query.MethodWrapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.black.core.aop.servlet.AopControllerIntercept.pageNumName;
import static com.black.core.aop.servlet.AopControllerIntercept.pageSizeName;

@Getter @Setter
public class HttpMethodWrapper {

    private final Collection<String> requestPaths;
    //text/plain;charset=UTF-8
    private final String contentType;
    private final Class<?> controllerClazz;
    private final Method httpMethod;
    private final MethodWrapper methodWrapper;
    private Object[] args;
    private Boolean page;
    private Boolean hasRequestBody;
    private String pageSizeArgName;
    private String pageNumArgName;


    public HttpMethodWrapper(Collection<String> requestPaths,
                             String contentType,
                             Class<?> controllerClazz,
                             Method httpMethod, Object[] args) {
        this.requestPaths = requestPaths;
        this.contentType = contentType;
        this.controllerClazz = controllerClazz;
        this.httpMethod = httpMethod;
        this.methodWrapper = MethodWrapper.get(httpMethod);
        this.args = args;
    }

    public HttpServletRequest getRequest(){
        return AopControllerIntercept.getRequest();
    }

    public Collection<ParameterWrapper> getParameterWrappers() {
        return methodWrapper.getParameterWrappersSet();
    }

    public boolean parameterHasAnnotation(Class<? extends Annotation> type){
        return methodWrapper.parameterHasAnnotation(type);
    }

    public ParameterWrapper getSinglonParameterByAnnotation(Class<? extends Annotation> targetAnnotation){
        return methodWrapper.getSingleParameterByAnnotation(targetAnnotation);
    }
    public List<ParameterWrapper> getParameterByAnnotation(Class<? extends Annotation> targetAnnotation){
        return methodWrapper.getParameterByAnnotation(targetAnnotation);
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
        return Arrays.toString(args);
    }

    public String showPath() {
        return Arrays.toString(requestPaths.toArray());
    }

    public boolean isPage() {
        if (page == null) {
            OpenIbatisPage ibatisPage = AnnotationUtils.getAnnotation(httpMethod, OpenIbatisPage.class);
            if (ibatisPage != null){
                page = true;
                if (pageNumName == null || pageSizeName == null || !ibatisPage.priority()) {
                    pageNumArgName = pageNumName;
                    pageSizeArgName = pageSizeName;
                }else {
                    pageNumArgName = ibatisPage.pageNum();
                    pageSizeArgName = ibatisPage.pageSize();
                }
            }else {
                page = false;
            }
        }
        return Boolean.TRUE.equals(page);
    }

}

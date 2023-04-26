package com.black.core.http.code;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.http.annotation.*;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
public class HttpProxyLayer implements AgentLayer {

    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        String prefix = AnnotationUtils.getAnnotation(layer.getAgentClazz(), OpenHttp.class).value();
        Method proxyMethod = layer.getProxyMethod();
        MethodWrapper mw = MethodWrapper.get(proxyMethod);
        Object[] args = layer.getArgs();
        Object result;
        GetUrl getUrl; PostUrl postUrl; PartUrl partUrl;
            //handle http get
        if ((getUrl = AnnotationUtils.getAnnotation(proxyMethod, GetUrl.class)) != null){
            String url = HttpUtils.parse(proxyMethod, prefix + getUrl.value(), args);
            Map<String, String> headerMap = HttpUtils.parseHeaders(proxyMethod, args);
            result = doHttp(proxyMethod, HttpMethod.GET, url, headerMap, null, null);
            //handle http post
        }else if ((postUrl = AnnotationUtils.getAnnotation(proxyMethod, PostUrl.class)) != null){
            String url = HttpUtils.parse(proxyMethod, prefix + postUrl.value(), args);
            String jsonBody = HttpUtils.parseJsonBody(proxyMethod, args);
            Map<String, String> headerMap = HttpUtils.parseHeaders(proxyMethod, args);
            result = doHttp(proxyMethod, HttpMethod.POST, url, headerMap, jsonBody, null);
            //handle http part
        }else if ((partUrl = AnnotationUtils.getAnnotation(proxyMethod, PartUrl.class)) != null){
            String url = HttpUtils.parse(proxyMethod, prefix + partUrl.value(), args);
            Map<String, String> headerMap = HttpUtils.parseHeaders(proxyMethod, args);
            List<ParameterWrapper> pws = mw.getParameterByAnnotation(HttpPart.class);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (ParameterWrapper pw : pws) {
                Object arg = args[pw.getIndex()];
                HttpPart annotation = pw.getAnnotation(HttpPart.class);
                String value = annotation.value();
                String name = StringUtils.hasText(value) ? value : pw.getName();
                if (arg == null){
                    builder.addTextBody(name, "");
                    continue;
                }
                Class<?> argClass = arg.getClass();
                if (argClass.isArray()) {
                    arg = SQLUtils.wrapList(arg);
                }
                if (arg instanceof Collection){
                    Collection<?> collection = (Collection<?>) arg;
                    for (Object ele : collection) {
                        addPart(name, ele, builder);
                    }
                }else {
                    addPart(name, arg, builder);
                }
            }
            result = doHttp(proxyMethod, HttpMethod.PART, url, headerMap, null, builder);
        } else {
            return layer.doFlow(layer.getArgs());
        }

        return result;
    }


    private void addPart(String name, Object value, MultipartEntityBuilder builder){
        if (value instanceof InputStream){
            builder.addBinaryBody(name, (InputStream) value);
            return;
        }

        if (value instanceof byte[]){
            builder.addBinaryBody(name, (byte[]) value);
            return;
        }

        if (value instanceof File){
            builder.addBinaryBody(name, (File) value);
            return;
        }

        if (value instanceof MultipartFile){
            try {
                builder.addBinaryBody(name, ((MultipartFile)value).getInputStream());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return;
        }

        String string = value.toString();
        ContentType contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8);
        StringBody stringBody = new StringBody(string, contentType);
        builder.addPart(name, stringBody);
    }

    public Object doHttp(Method method, HttpMethod httpMethod, String url,
                         Map<String, String> headers, String jsonBody, MultipartEntityBuilder builder){
        if (log.isInfoEnabled()) {
            log.info("do {}, url: {}", httpMethod.getName(), url);
        }
        Class<?> returnType = method.getReturnType();
        MethodWrapper mw = MethodWrapper.get(method);
        boolean ssl = mw.hasAnnotation(SSL.class) || mw.getDeclaringClassWrapper().hasAnnotation(SSL.class);
        if (ssl){
            HttpUtils.useSSL();
        }
        try {
            HttpResponseWrapper httpResponse;
            Object result;
            switch (httpMethod){
                case GET:
                    httpResponse = HttpUtils.sendGetJson(url, headers);
                    break;
                case POST:
                    httpResponse = HttpUtils.sendPostJson(url, jsonBody, headers);
                    break;
                case PART:
                    httpResponse = HttpUtils.sendPostPart(url, builder);
                    break;
                default:
                    throw new IllegalStateException("unknown http method :" + httpMethod);
            }
            String response = httpResponse.body();
            result = response;
            if (!returnType.equals(String.class)){
                TypeHandler typeHandler = TypeConvertCache.initAndGet();
                if (typeHandler != null){
                    result = typeHandler.convert(returnType, response);
                }
            }else {
                result = response;
            }
            return result;
        }finally {
            HttpUtils.finishFetch();
        }
    }
}

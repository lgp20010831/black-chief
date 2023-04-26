package com.black.api;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.black.api.ApiV2Utils.*;
import static com.black.api.ApiV2Utils.METHOD_WRAPPER_NAME;

public class SpringRequestParamAutoFindLocutor extends AbstractApiLocutor{

    @Override
    public void interludeHttpMethod(String item, Map<String, Object> source, HttpMethod method) {
        String requestUrlString = method.getRequestUrlString();
        if (item.startsWith("url: ") || (requestUrlString != null && requestUrlString.contains("?"))){
            return;
        }
        AliasColumnConvertHandler handler = (AliasColumnConvertHandler) source.get(ALIAS_COLUMN_NAME);
        Class<?> controllerType  = (Class<?>) source.get(CONTROLLER_TYPE_NAME);
        MethodWrapper mw = (MethodWrapper) source.get(METHOD_WRAPPER_NAME);
        StringJoiner joiner = new StringJoiner("&", "?", "");
        int size = 0;
        for (ParameterWrapper pw : mw.getParameterWrappersSet()) {
            boolean isUrlParam = pw.getAnnotationSize() == 0;
            String name = pw.getName();
            RequestParam annotation = pw.getAnnotation(RequestParam.class);
            if (annotation != null){
                isUrlParam = true;
                if (StringUtils.hasText(annotation.value())) {
                    name = annotation.value();
                }
            }
            if (isUrlParam){
                Object demo = getDemoByType(pw.getType());
                joiner.add(name + "=" + demo);
                size ++;
            }
        }

        if (size > 0){
            final String param = joiner.toString();
            List<String> requestUrl = method.getRequestUrl();
            method.setRequestUrl(StreamUtils.mapList(requestUrl, url -> url + param));
        }
    }
}

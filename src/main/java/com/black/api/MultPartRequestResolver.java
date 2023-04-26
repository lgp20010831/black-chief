package com.black.api;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public class MultPartRequestResolver {


    public void resolverMultPartRequest(HttpMethod httpMethod, MethodWrapper mw){
        httpMethod.setMulitPartRequest(true);
        List<ParameterWrapper> pws = mw.getParameterByAnnotation(RequestPart.class);
        String boundary = ApiV2Utils.createBoundary();
        String endBoundry = boundary + "--";
        StringJoiner joiner = new StringJoiner("\n", "", "\n" + endBoundry);
        for (ParameterWrapper pw : pws) {
            RequestPart annotation = pw.getAnnotation(RequestPart.class);
            String name = pw.getName();
            String key = StringUtils.hasText(annotation.value()) ? annotation.value() : name;
            Class<?> type = pw.getType();
             if (Collection.class.isAssignableFrom(type)){
                Class<?>[] genericVals = ReflectionUtils.getMethodParamterGenericVals(pw.getParameter());
                Class<?> genType = ServiceUtils.arrayIndex(genericVals, 0);
                joiner.add(getDemoOfType(genType, boundary, key));
                joiner.add("....");
            }else {
                 joiner.add(getDemoOfType(type, boundary, key));
             }
        }
        String requestDemo = joiner.toString();
        httpMethod.setRequestDome(requestDemo);
        httpMethod.setRequestInvokeDome(requestDemo);
    }

    protected String getDemoOfType(Class<?> type, String boundary, String key){
        if (MultipartFile.class.isAssignableFrom(type)){
            return createPart(key, "文件流", boundary, true);
        }else {
            Object demoByType = ApiV2Utils.getDemoByType(type);
            return createPart(key, String.valueOf(demoByType), boundary, false);
        }
    }


    public static String createPart(String name, String content, String boundary, boolean isFile){

        StringJoiner joiner = new StringJoiner("\n", boundary + "\n", "");
        String desc = StringUtils.linkStr("Content-Disposition: form-data; name=\"", name, "\";", isFile ?
                "Content-Type: uncertain specific file type" : "");
        joiner.add(desc);
        joiner.add("\n");
        joiner.add(content);
        return joiner.toString();
    }

}

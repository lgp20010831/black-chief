package com.black.xml.servlet;

import com.black.core.util.StringUtils;
import com.black.generic.GenericInfo;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import lombok.*;

import java.util.*;

/**
 * @author 李桂鹏
 * @create 2023-05-31 14:11
 */
@SuppressWarnings("all") @Data
public class MappingMethodInfo {

    private final Map<String, Object> requestMappingInfos = new LinkedHashMap<>();

    private final Map<String, Object> apiOperationInfos = new LinkedHashMap<>();

    private boolean openPage = false;

    private boolean voidReturnType = false;

    private boolean onV2Swagger = false;

    private String v2SwaggerValue = "";

    private String methodName;

    private GenericInfo methodGenericInfo;

    private List<RequestParamInfo> paramInfos = new ArrayList<>();

    private String body;

    private CtAnnotations methodAnnotations = new CtAnnotations();

    public void addParam(@NonNull RequestParamInfo paramInfo){
        paramInfos.add(paramInfo);
    }

    public void addParam(String name, Class<?> type, boolean required, ParamPart paramPart){
        paramInfos.add(new RequestParamInfo(name, type, required, paramPart));
    }

    public void addMethodAnnotation(CtAnnotation... ctAnnotations){
        methodAnnotations.addAnnotations(Arrays.asList(ctAnnotations));
    }

    public boolean hasBodyParam(){
        for (RequestParamInfo paramInfo : paramInfos) {
            ParamPart paramPart = paramInfo.paramPart;
            if (paramPart == ParamPart.RequestPart || paramPart == ParamPart.RequestBody){
                return true;
            }
        }
        return false;
    }

    @Getter @Setter
    public static class RequestParamInfo{
        private final String name;
        private final Class<?> type;
        private boolean required = true;
        private ParamPart paramPart;
        private CtAnnotations annotations;
        private String genericDesc;

        public RequestParamInfo(String name, Class<?> type, boolean required, ParamPart paramPart) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.paramPart = paramPart;
            this.annotations = new CtAnnotations();
        }

        public void addAnnotations(CtAnnotation... annotations){
            this.annotations.addAnnotations(Arrays.asList(annotations));
        }

        @Override
        public String toString() {
            String annName = paramPart.getAnnName();
            String annAttr = paramPart != ParamPart.RequestBody ?
                    (required ? StringUtils.letString("(required=true, value=", name, ")") :
                            StringUtils.letString("(required=false, value=", name, ")"))
                    :
                    (required ? "(required=true)" : "(required=false)");
            return StringUtils.letString(annName, annAttr, annotations.isEmpty() ? " " :"\n", annotations, " ", type.getSimpleName(),
                   genericDesc, " ");
        }
    }

}

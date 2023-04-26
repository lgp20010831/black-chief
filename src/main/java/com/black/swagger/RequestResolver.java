package com.black.swagger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.FormatParser;
import com.black.blent.Blent;
import com.black.blent.BlentJavassistManager;
import com.black.blent.BlentUtils;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.core.bean.TrustBeanCollector;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import javassist.CtField;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

 @SuppressWarnings("all")
public class RequestResolver {

    private static final IoLog log = LogFactory.getLog4j();

    public static final String DEFAULT_FLAG = "$R:";

    public static String DEFAULT_JAVA_CREATE_CLASS_NAME = "SwaggerChiefAnalytic";

    private static final AtomicInteger sort = new AtomicInteger(0);

    private final FormatParser formatParser;

    public RequestResolver() {
        formatParser = new FormatParser();
    }

     public Class<?> parseRequest(String request){
        return parseRequest(request, null);
     }

    public Class<?> parseRequest(String request, Class<?> assist){
        String[] parts = request.split("\\+");
        JSONObject json = new JSONObject();
        PartiallyCtClass partiallyCtClass = null;
        for (String part : parts) {
            part = StringUtils.removeFrontSpace(part);
            if (part.startsWith(DEFAULT_FLAG) || part.startsWith("{") || part.startsWith("[")){
                part = StringUtils.removeIfStartWith(part, DEFAULT_FLAG);
                JSON def_json = parseJson(part);
                if (def_json instanceof JSONArray){
                    throw new UnsupportedOperationException("not support to convert array");
                }
                if (def_json instanceof JSONObject){
                    json.putAll((Map<? extends String, ?>) def_json);
                }
            }else {
                Blent blent = BlentUtils.parseBlends(part);
                if (!blent.isJson()) {
                    throw new UnsupportedOperationException("not support blent to convert array");
                }
                if (partiallyCtClass != null){
                    throw new IllegalStateException("only support one blent");
                }
                partiallyCtClass = parseBlentToPartiallyClass(blent, assist);
            }
        }

        if (partiallyCtClass == null){
            //没有 blent 表达式, 则生成一个默认的虚拟类
            partiallyCtClass = PartiallyCtClass.make(DEFAULT_JAVA_CREATE_CLASS_NAME + sort.incrementAndGet());
        }

        List<CtField> fields = mutateJsonToFields(json, partiallyCtClass);
        partiallyCtClass.addAllField(fields);
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        log.info("create ct java class:{}", javaClass.getSimpleName());
        return javaClass;
    }

    protected List<CtField> mutateJsonToFields(JSONObject json, PartiallyCtClass partiallyCtClass){
        return Utils.mutateJsonToFields(json, partiallyCtClass.getCtClass(), (field, ctClass, jsonValue) -> {
            String remark = jsonValue == null ? "" : jsonValue.toString();
            Utils.addAnnotationOnField(field, ctClass, ApiModelProperty.class, "value", remark);
        });
    }

    protected PartiallyCtClass parseBlentToPartiallyClass(Blent blent, Class<?> assist){
        return BlentJavassistManager.parseBlentToPartiallyClass(blent, TrustBeanCollector::getTrustBean);
    }

    private JSON parseJson(String part){
        return formatParser.parseJSON(part);
    }
}

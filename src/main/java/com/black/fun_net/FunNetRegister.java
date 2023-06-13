package com.black.fun_net;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.generic.GenericInfo;
import com.black.generic.GenericUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.xml.servlet.GeneratorInfo;
import com.black.xml.servlet.MappingMethodInfo;
import com.black.xml.servlet.MvcGenerator;
import com.black.xml.servlet.ServletGenerateUtils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class FunNetRegister {

    public static final String FUN_NET_CLASS_PREFIX = "FunNetServlet";

    public static final IoLog log = LogFactory.getLog4j();

    private static FunNetRegister register;

    public synchronized static FunNetRegister getInstance() {
        if (register == null){
            register = new FunNetRegister();
        }
        return register;
    }

    private FunNetRegister(){

    }

    public <T extends Net> Object loadClass(Class<T> netClass){
        check(netClass);
        Net net = InstanceBeanManager.instance(netClass, InstanceType.BEAN_FACTORY_SINGLE);
        ClassWrapper<?> classWrapper = ClassWrapper.get(netClass);
        Signature annotation = classWrapper.getAnnotation(Signature.class);
        String desc = annotation == null ? "" : annotation.value();
        String[] descParts = StringUtils.hasText(desc) ? desc.split("\\|") : new String[0];
        RequestMapping mapping = AnnotationUtils.findAnnotation(netClass, RequestMapping.class);
        Map<String, Object> mappingInfos;
        if (mapping == null){
            mappingInfos = new LinkedHashMap<>();
        }else {
            mappingInfos = AnnotationUtils.getAnnotationValueMap(mapping, false);
        }
        String[] urls = mapping == null ? (descParts.length >= 1 ? descParts[0].trim().split(",") : new String[0])
                : mapping.value();

        List<FieldWrapper> servlets = classWrapper.getFieldByType(Servlet.class);
        MvcGenerator generator = new MvcGenerator(FUN_NET_CLASS_PREFIX);
        for (FieldWrapper fieldWrapper : servlets) {
            handlerServletInfo(fieldWrapper, generator, net);
        }
        //组装类的注解
        CtAnnotation apiAnnotation = createApiAnnotation(classWrapper, descParts);
        CtAnnotations classAnnotations = getClassAnnotations(classWrapper);
        CtAnnotations annotations = CtAnnotations.group(apiAnnotation);
        annotations.addAnnotations(classAnnotations.getAnnotationList());

        generator.generateServletAnnotation(mappingInfos, annotations, urls);
        GeneratorInfo generatorInfo = generator.registerMvc();
        String classInfo = generatorInfo.getClassInfo();
        Object instance = generatorInfo.getInstance();
        log.info("Generating functional network interfaces: \n{}", classInfo);
        return ApplyProxyFactory.proxy(instance, new NetServletHandler(net), true);
    }

    protected CtAnnotation createApiAnnotation(ClassWrapper<?> classWrapper, String[] descParts){
        if (descParts.length >= 2){
            CtAnnotation ctAnnotation = new CtAnnotation(Api.class);
            ctAnnotation.addField("tags", descParts[1].trim());
            return ctAnnotation;
        }
        Api annotation = classWrapper.getAnnotation(Api.class);
        if (annotation != null){
            return new CtAnnotation(annotation);
        }else {
            CtAnnotation ctAnnotation = new CtAnnotation(Api.class);
            ctAnnotation.addField("tags", classWrapper.getSimpleName());
            return ctAnnotation;
        }
    }

    protected CtAnnotations getClassAnnotations(ClassWrapper<?> classWrapper){
        CtAnnotations ctAnnotations = new CtAnnotations();
        for (Annotation annotation : classWrapper.getAnnotationMap().values()) {
            if (!annotation.annotationType().equals(Api.class)){
                ctAnnotations.addAnnotation(new CtAnnotation(annotation));
            }
        }
        return ctAnnotations;
    }

    protected void handlerServletInfo(FieldWrapper fw, MvcGenerator generator, Object instance){
        Class<?> type = fw.getType();
        GenericInfo genericInfo = GenericUtils.getGenericByField(fw.getField());
        Servlet servlet = (Servlet) fw.getValue(instance);
        String desc = getDesc(fw, servlet);
        String[] parts = desc.split("\\|");
        MappingMethodInfo methodInfo = new MappingMethodInfo();
        methodInfo.setMethodGenericInfo(genericInfo);

        //构造 requestMapping infos
        Map<String, Object> requestMappingInfos = methodInfo.getRequestMappingInfos();
        requestMappingInfos.put("method", parseMethod(servlet, type));
        requestMappingInfos.put("value", parsePath(fw));
        handlerPronMapping(requestMappingInfos, fw);

        Map<String, Object> apiOperationInfos = methodInfo.getApiOperationInfos();
        apiOperationInfos.put("value", parseApiOperation(parts));

        CtAnnotations methodAnnotations = methodInfo.getMethodAnnotations();
        parseMethodAnnotations(parts, methodAnnotations);

        methodInfo.setOnV2Swagger(false);
        methodInfo.setVoidReturnType(false);
        methodInfo.setOpenPage(false);
        methodInfo.setMethodName(fw.getName());
        methodInfo.setBody("{throw new IllegalStateException(\"wrong execution to this point\");}");
        parseParam(parts, methodInfo);
        generator.addRequestMethod(methodInfo);
    }


    protected String getDesc(FieldWrapper fw, Servlet servlet){
        Signature annotation = fw.getAnnotation(Signature.class);
        String desc = null;
        if (servlet != null){
            desc = servlet.getDesc();
        }
        if (desc == null){
            desc = annotation == null ? "" : annotation.value();
        }
        return desc;
    }

    protected void handlerPronMapping(Map<String, Object> requestMappingInfos, FieldWrapper fw){
        PronMapping annotation = fw.getAnnotation(PronMapping.class);
        if (annotation != null){
            String[] value = annotation.value();
            for (String txt : value) {
                String[] kv = txt.split(":");
                requestMappingInfos.put(kv[0].trim(), kv[1].trim());
            }
        }
    }

    protected void parseMethodAnnotations(String[] parts, CtAnnotations methodAnnotations){
        if (parts.length >= 3){
            CtAnnotations ctAnnotations = ServletGenerateUtils.parseAnnotations(parts[2].trim());
            methodAnnotations.addAnnotations(ctAnnotations.getAnnotationList());
        }
    }

    protected void parseParam(String[] parts, MappingMethodInfo methodInfo){
        if (parts.length >= 1){
            String txt = parts[0].trim();
            String[] params = txt.split(",");
            for (String param : params) {
                if (StringUtils.hasText(param)){
                    ServletGenerateUtils.parseParam2(param, methodInfo);
                }
            }
        }

    }

    protected String parseApiOperation(String[] parts){
        if (parts.length >= 2){
            return parts[1].trim();
        }
        return "";
    }

    protected String[] parsePath(FieldWrapper fw){
        return new String[]{fw.getName()};
    }

    protected RequestMethod parseMethod(Servlet servlet, Class<?> type){
        if (servlet != null){
            return servlet.getRequestMethod();
        }else {
            if (Post.class.isAssignableFrom(type)){
                return RequestMethod.POST;
            }else if (Get.class.isAssignableFrom(type)){
                return RequestMethod.GET;
            }else if(Delete.class.isAssignableFrom(type)){
                return RequestMethod.DELETE;
            }else if (Put.class.isAssignableFrom(type)){
                return RequestMethod.PUT;
            }else {
                return RequestMethod.GET;
            }
        }
    }

    protected void check(Class<?> netClass){
        if (!Net.class.isAssignableFrom(netClass)){
            throw new IllegalStateException("Functional network interface needs to inherit Net.class");
        }
    }
}

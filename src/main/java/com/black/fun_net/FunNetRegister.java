package com.black.fun_net;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.xml.servlet.GeneratorInfo;
import com.black.xml.servlet.MappingMethodInfo;
import com.black.xml.servlet.MvcGenerator;
import com.black.xml.servlet.ServletGenerateUtils;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
        RequestMapping mapping = AnnotationUtils.findAnnotation(netClass, RequestMapping.class);
        Map<String, Object> mappingInfos;
        if (mapping == null){
            mappingInfos = new LinkedHashMap<>();
        }else {
            mappingInfos = AnnotationUtils.getAnnotationValueMap(mapping, false);
        }
        String[] urls = mapping == null ? new String[0] : mapping.value();
        ClassWrapper<?> classWrapper = ClassWrapper.get(netClass);
        List<FieldWrapper> servlets = classWrapper.getFieldByType(Servlet.class);
        MvcGenerator generator = new MvcGenerator(FUN_NET_CLASS_PREFIX);
        for (FieldWrapper fieldWrapper : servlets) {
            handlerServletInfo(fieldWrapper, classWrapper, generator);
        }
        CtAnnotation apiAnnotation = createApiAnnotation(classWrapper);
        CtAnnotations annotations = CtAnnotations.group(apiAnnotation);
        generator.generateServletAnnotation(mappingInfos, annotations, urls);
        GeneratorInfo generatorInfo = generator.registerMvc();
        String classInfo = generatorInfo.getClassInfo();
        Object instance = generatorInfo.getInstance();
        log.info("Generating functional network interfaces: \n{}", classInfo);
        return ApplyProxyFactory.proxy(instance, new NetServletHandler(net));
    }

    protected CtAnnotation createApiAnnotation(ClassWrapper<?> classWrapper){
        Api annotation = classWrapper.getAnnotation(Api.class);
        if (annotation != null){
            return new CtAnnotation(annotation);
        }else {
            CtAnnotation ctAnnotation = new CtAnnotation(Api.class);
            ctAnnotation.addField("tags", classWrapper.getSimpleName());
            return ctAnnotation;
        }
    }

    protected void handlerServletInfo(FieldWrapper fw, ClassWrapper<?> cw, MvcGenerator generator){
        Class<?> type = fw.getType();
        MappingMethodInfo methodInfo = new MappingMethodInfo();
        //构造 requestMapping infos
        Map<String, Object> requestMappingInfos = methodInfo.getRequestMappingInfos();
        requestMappingInfos.put("method", parseMethod(type));
        requestMappingInfos.put("value", parsePath(fw));

        Map<String, Object> apiOperationInfos = methodInfo.getApiOperationInfos();
        apiOperationInfos.put("value", parseApiOperation(fw));

        methodInfo.setOnV2Swagger(false);
        methodInfo.setVoidReturnType(false);
        methodInfo.setOpenPage(false);
        methodInfo.setMethodName(fw.getName());
        methodInfo.setBody("{throw new IllegalStateException(\"wrong execution to this point\");}");
        parseParam(fw, methodInfo);
        generator.addRequestMethod(methodInfo);
    }

    protected void parseParam(FieldWrapper fw, MappingMethodInfo methodInfo){
        ParamDesc annotation = fw.getAnnotation(ParamDesc.class);
        if (annotation != null){
            String txt = annotation.value();
            String[] params = txt.split(",");
            for (String param : params) {
                ServletGenerateUtils.parseParam2(param, methodInfo);
            }
        }
    }

    protected String parseApiOperation(FieldWrapper fw){
        NetDesc annotation = fw.getAnnotation(NetDesc.class);
        return annotation == null ? "" : annotation.value();
    }

    protected String[] parsePath(FieldWrapper fw){
        return new String[]{fw.getName()};
    }

    protected RequestMethod parseMethod(Class<?> type){
        if (Post.class.isAssignableFrom(type)){
            return RequestMethod.POST;
        }else if (Get.class.isAssignableFrom(type)){
            return RequestMethod.GET;
        }else {
            throw new IllegalStateException("Unrecognized network interface method");
        }
    }

    protected void check(Class<?> netClass){
        if (!Net.class.isAssignableFrom(netClass)){
            throw new IllegalStateException("Functional network interface needs to inherit Net.class");
        }
    }
}

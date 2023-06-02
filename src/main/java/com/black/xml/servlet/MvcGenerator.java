package com.black.xml.servlet;

import com.black.core.annotation.ChiefServlet;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.core.util.TextUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.mvc.MvcMappingRegister;
import com.black.swagger.v2.V2Swagger;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;
import io.swagger.annotations.ApiOperation;
import javassist.ClassPool;
import javassist.CtMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-05-31 14:03
 */
@SuppressWarnings("all")
public class MvcGenerator {

    private final IoLog log = LogFactory.getArrayLog();

    public static boolean print = false;

    private StringBuilder methodInfoBuilder = new StringBuilder();

    private StringBuilder classInfoBuilder = new StringBuilder();

    private CtAnnotations classAnnotations = new CtAnnotations();

    private final PartiallyCtClass partiallyCtClass;

    public MvcGenerator(PartiallyCtClass partiallyCtClass){
        this.partiallyCtClass = partiallyCtClass;
        imports();
    }

    public MvcGenerator(String classPrefix) {
        partiallyCtClass = PartiallyCtClass.make(classPrefix + IdUtils.createShort8Id());
        imports();
    }

    protected void imports(){
        importDependencyPackage("java.lang", "java.util", "java.net", "java.io", "java.math",
                "java.sql", "java.time");

    }

    protected String prepareCode(String code){
        if (!StringUtils.hasText(code)){
            return "{}";
        }

        code = StringUtils.addIfNotStartWith(code, "{");
        code = StringUtils.addIfNotEndWith(code, "}");
        code = ServiceUtils.parseTxt(code, "$[", "]", imports -> {
            String[] splits = imports.split(",");
            for (String split : splits) {
                importDependencyPackage(split.trim());
            }
            return "";
        });
        code = ServiceUtils.parseTxt(code, "${", "}", txt -> {
            return "\"" + txt + "\"";
        });
        return code;
    }

    public void importDependencyPackage(String... packages) {
        ClassPool pool = Utils.getPool();
        for (String name : packages) {
            pool.importPackage(name);
        }
    }

    public PartiallyCtClass getPartiallyCtClass() {
        return partiallyCtClass;
    }

    public void generateServletAnnotation(Map<String, Object> attrs, CtAnnotations others, String... paths){
        CtAnnotation servletAnn = new CtAnnotation(ChiefServlet.class);
        servletAnn.addField("value", paths, String[].class);
        ServletGenerateUtils.loadCtAnnotationAttrs(servletAnn, attrs);
        if (others == null){
            others = new CtAnnotations();
        }
        others.addAnnotation(servletAnn);
        classAnnotations.addAnnotations(others.getAnnotationList());

    }

    public void addOtherAnnotations(CtAnnotations annotations){
        classAnnotations.addAnnotations(annotations.getAnnotationList());
    }

    public void setSuperClass(Class<?> superClass){
        partiallyCtClass.setSuperClass(superClass);
    }

    public CtMethod addRequestMethod(MappingMethodInfo mappingMethodInfo){
        String methodName = mappingMethodInfo.getMethodName();
        Class<?> returnType = mappingMethodInfo.isVoidReturnType() ? void.class : Object.class;
        List<MappingMethodInfo.RequestParamInfo> paramInfos = mappingMethodInfo.getParamInfos();
        String body = mappingMethodInfo.getBody();
        List<? extends Class<?>> paramTypes = StreamUtils.mapList(paramInfos, MappingMethodInfo.RequestParamInfo::getType);
        CtMethod ctMethod = partiallyCtClass.addMethod(methodName, returnType, body, paramTypes.toArray(new Class[0]));

        //添加参数注解
        for (int i = 0; i < paramInfos.size(); i++) {
            MappingMethodInfo.RequestParamInfo paramInfo = paramInfos.get(i);
            String name = paramInfo.getName();
            CtAnnotation annotation;
            if (paramInfo.isQuery()){
                annotation = new CtAnnotation(RequestParam.class);
                annotation.addField("value", name, String.class);
            }else {
                annotation = new CtAnnotation(RequestBody.class);
            }
            annotation.addField("required", paramInfo.isRequired(), boolean.class);
            partiallyCtClass.addParameterAnnotation(methodName, i, CtAnnotations.group(annotation));
        }
        //添加方法注解
        CtAnnotation requestMappingAnn = new CtAnnotation(RequestMapping.class);
        ServletGenerateUtils.loadCtAnnotationAttrs(requestMappingAnn, mappingMethodInfo.getRequestMappingInfos());

        CtAnnotation apiOperationAnn = new CtAnnotation(ApiOperation.class);
        ServletGenerateUtils.loadCtAnnotationAttrs(apiOperationAnn, mappingMethodInfo.getApiOperationInfos());

        CtAnnotations ctAnnotations = CtAnnotations.group(requestMappingAnn, apiOperationAnn);
        if (mappingMethodInfo.isOnV2Swagger()){
            CtAnnotation v2SwaggerAnn = new CtAnnotation(V2Swagger.class);
            v2SwaggerAnn.addField("value", mappingMethodInfo.getV2SwaggerValue(), String.class);
            ctAnnotations.addAnnotation(v2SwaggerAnn);
        }

        if (mappingMethodInfo.isOpenPage()){
            ctAnnotations.addAnnotation(new CtAnnotation(OpenSqlPage.class));
        }

        methodInfoBuilder.append("\n\n");
        StringJoiner joiner = new StringJoiner(", ");
        int i = 1;
        for (MappingMethodInfo.RequestParamInfo info : paramInfos) {
            joiner.add(StringUtils.letString(info.toString(), "$", i++));
        }
        String methodInfo = TextUtils.parseContent("{}{} {}({})\n{}", ctAnnotations,
                returnType.getSimpleName(), methodName, joiner.toString(), body);
        methodInfoBuilder.append(StringUtils.overallIndent(methodInfo, 4));
        if (print){
            log.debug("Generated Request Method:\n{}", methodInfo);
        }
        partiallyCtClass.addMethodAnnotations(methodName, ctAnnotations);
        return ctMethod;
    }

    public GeneratorInfo registerMvc(){
        partiallyCtClass.addClassAnnotations(classAnnotations);
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        Class<?> superclass = javaClass.getSuperclass();
        superclass = superclass == null ? Object.class : superclass;
        String classInfo = TextUtils.parseContent("{}public class {} extends {}", classAnnotations,
                javaClass.getSimpleName(), superclass.getSimpleName());
        classInfoBuilder.append(classInfo);
        ClassWrapper<?> wrapper = ClassWrapper.get(javaClass);
        Object instance = MvcMappingRegister.registerSupportAopController(javaClass);
        String classCompleteInfo = createClassCompleteInfo();
        return new GeneratorInfo(instance, javaClass, classCompleteInfo);
    }

    protected String createClassCompleteInfo(){
        return StringUtils.letString(classInfoBuilder, "{", "\n", methodInfoBuilder, "\n", "}");
    }
}

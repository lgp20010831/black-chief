package com.black.xml.servlet;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.StringUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-05-31 13:42
 */
@SuppressWarnings("all")
public class XmlServletRegister implements ElementWrapperHandler{

    public static final String DEF_PREFIX = "XmlServlet";

    private final IoLog log = LogFactory.getArrayLog();

    public static volatile boolean debugPrint = true;

    private static XmlServletRegister register;

    static {

    }

    public synchronized static XmlServletRegister getInstance() {
        if (register == null) register = new XmlServletRegister();
        return register;
    }

    public void parseWrapper(String name, ElementWrapper ew){
        parseWrapper(name, DEF_PREFIX, ew);
    }

    public void parseWrapper(String name, String classNamePrefix, ElementWrapper ew){
        if (!ew.getAttrMap().containsKey("mapping")) {
            return;
        }
        MvcGenerator generator = createServletClass(classNamePrefix, name, ew);
        Map<String, List<ElementWrapper>> elements = ew.getElements();
        for (String key : elements.keySet()) {
            if ("select".equalsIgnoreCase(key)){
                parseSelectWrapper(name, generator, elements.get(key));
            }else if ("update".equalsIgnoreCase(key)){
                parseUpdateWrapper(name, generator, elements.get(key));
            }
        }
        GeneratorInfo info = generator.registerMvc();
        if (debugPrint){
            log.info("Dynamically generated controller info:\n{}", info.getClassInfo());
        }
    }

    protected void parseSelectWrapper(String name, MvcGenerator generator, List<ElementWrapper> wrappers){
        for (ElementWrapper wrapper : wrappers) {
            MappingMethodInfo methodInfo = parseSelectWrapperInfo(name, wrapper);
            generator.addRequestMethod(methodInfo);
        }
    }

    protected void parseUpdateWrapper(String name, MvcGenerator generator, List<ElementWrapper> wrappers){
        for (ElementWrapper wrapper : wrappers) {
            MappingMethodInfo methodInfo = parseUpdateWrapperInfo(name, wrapper);
            generator.addRequestMethod(methodInfo);
        }
    }

    protected MappingMethodInfo parseSelectWrapperInfo(String name, ElementWrapper wrapper){
        String id = getAttribute(wrapper, "id");
        MappingMethodInfo methodInfo = new MappingMethodInfo();
        methodInfo.setMethodName(id);
        methodInfo.setVoidReturnType(false);
        //构造参数
        int paramSize = createParamInfo(wrapper, methodInfo);
        createAnnInfo(wrapper, methodInfo, methodInfo.hasBodyParam() ? "POST" : "GET");

        //构造body
        String result = getAttribute(wrapper, "result", "list()");
        result = StringUtils.addIfNotEndWith(result, ";");
        String before = "", after = "";
        ElementWrapper beforeWrapper = wrapper.getByName("before");
        if (beforeWrapper != null){
            before = beforeWrapper.getAttrVal("value") == null ? beforeWrapper.getStringValue() :
                    beforeWrapper.getAttrVal("value");
            beforeWrapper.clearContent();
        }
        ElementWrapper afterWrapper = wrapper.getByName("after");
        if (afterWrapper != null){
            after = afterWrapper.getAttrVal("value") == null ? afterWrapper.getStringValue() :
                    afterWrapper.getAttrVal("value");
            afterWrapper.clearContent();
        }

        String selectBody = "  Object result = XmlSql.opt(\"" + name + "\").select(\"" + id + "\", new Object[]{" + createInParams(paramSize) + "})." + result;
        String body = StringUtils.letString(
                "{\n", before, selectBody, after,
                "return result;", "\n}"
        );
        methodInfo.setBody(body);
        return methodInfo;
    }

    protected String createInParams(int size){
        StringJoiner joiner = new StringJoiner(", ", "", "");
        for (int i = 1; i < size + 1; i++) {
            joiner.add("$" + i);
        }
        return joiner.toString();
    }

    protected void createAnnInfo(ElementWrapper wrapper, MappingMethodInfo methodInfo, String dfMethod){
        String id = getAttribute(wrapper, "id");
        String url = getAttribute(wrapper, "url", id);
        String[] urls = url.split(",");
        String method = getAttribute(wrapper, "method", dfMethod);
        RequestMethod requestMethod = RequestMethod.valueOf(method.toUpperCase());
        //构造 @RequestMapping 注解信息
        Map<String, Object> requestMappingInfos = methodInfo.getRequestMappingInfos();
        requestMappingInfos.put("value", urls);
        requestMappingInfos.put("method", requestMethod);
        ServletGenerateUtils.putAllAttrs(requestMappingInfos, wrapper.getAttrMap());
        //构造 @ApiOperation 注解信息
        Map<String, Object> apiOperationInfos = methodInfo.getApiOperationInfos();
        String remark = getAttribute(wrapper, "remark", "");
        apiOperationInfos.put("value", remark);
        //构造 @V2Swagger 注解信息
        String response = wrapper.getAttrVal("response");
        if (response != null){
            methodInfo.setOnV2Swagger(true);
            methodInfo.setV2SwaggerValue(response);
        }
        //构造 page信息
        String page = getAttribute(wrapper, "page", "false");
        boolean isPage = Boolean.parseBoolean(page);
        methodInfo.setOpenPage(isPage);
    }

    protected int createParamInfo(ElementWrapper wrapper, MappingMethodInfo methodInfo){
        //构造参数
        String params = getAttribute(wrapper, "request", "!body::body");
        String[] paramArray = params.split(",");
        for (String param : paramArray) {
            ServletGenerateUtils.parseParam2(param, methodInfo);
        }
        return paramArray.length;
    }

    protected MappingMethodInfo parseUpdateWrapperInfo(String name, ElementWrapper wrapper){
        String id = getAttribute(wrapper, "id");
        MappingMethodInfo methodInfo = new MappingMethodInfo();
        methodInfo.setMethodName(id);
        methodInfo.setVoidReturnType(true);
        //构造参数
        int paramSize = createParamInfo(wrapper, methodInfo);
        createAnnInfo(wrapper, methodInfo, "POST");

        //构造body
        String before = "", after = "";
        ElementWrapper beforeWrapper = wrapper.getByName("before");
        if (beforeWrapper != null){
            before = beforeWrapper.getAttrVal("value") == null ? beforeWrapper.getStringValue() :
                    beforeWrapper.getAttrVal("value");
            beforeWrapper.clearContent();
        }
        ElementWrapper afterWrapper = wrapper.getByName("after");
        if (afterWrapper != null){
            after = afterWrapper.getAttrVal("value") == null ? afterWrapper.getStringValue() :
                    afterWrapper.getAttrVal("value");
            afterWrapper.clearContent();
        }

        String selectBody = "  XmlSql.opt(\"" + name + "\").update(\"" + id + "\", new Object[]{" + createInParams(paramSize) + "});";
        String body = StringUtils.letString(
                "{\n", before, selectBody, after, "\n}"
        );
        methodInfo.setBody(body);
        return methodInfo;
    }


    protected MvcGenerator createServletClass(String classNamePrefix, String name, ElementWrapper ew){
        String mapping = getAttribute(ew, "mapping", "");
        String remark = getAttribute(ew, "remark", "");
        CtAnnotation apiAnn = new CtAnnotation(Api.class);
        apiAnn.addField("tags", remark, String[].class);
        String[] mappings = mapping.split(",");
        String[] urls = new String[mappings.length];
        for (int i = 0; i < mappings.length; i++) {
            String url = mappings[i];
            url = StringUtils.removeIfStartWith(url, "/");
            url = name + "/" + url;
            urls[i] = url;
        }
        MvcGenerator generator = new MvcGenerator(classNamePrefix);
        generator.importDependencyPackage("com.alibaba.fastjson", "com.black.sql_v2", "com.black.xml");
        generator.generateServletAnnotation(ew.getAttrMap(), CtAnnotations.group(apiAnn), urls);
        return generator;
    }


}

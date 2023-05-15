package com.black.template.core;

import com.alibaba.fastjson.JSONObject;
import com.black.core.api.ApiUtil;
import com.black.core.json.JsonUtil;
import com.black.core.json.JsonUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author 李桂鹏
 * @create 2023-05-15 16:01
 */
@SuppressWarnings("all")
public class ThymeleafTemplateResolver implements TemplateResolver{

    private final TemplateEngine templateEngine;

    public ThymeleafTemplateResolver(String... resolvablePatterns) {
        templateEngine = new TemplateEngine();
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        if (resolvablePatterns != null){
            templateResolver.setResolvablePatterns(new HashSet<>(Arrays.asList(resolvablePatterns)));
        }
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        templateEngine.addTemplateResolver(templateResolver);
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    @Override
    public String resolve(String name, Object data) {
        JSONObject json = JsonUtils.letJson(data);
        Context context = ApiUtil.createContext(json);
        return templateEngine.process(name, context);
    }

    @Override
    public void resolveOutputStream(String name, Object data, OutputStream outputStream) {
        resolveWriter(name, data, new OutputStreamWriter(outputStream));
    }

    @Override
    public void resolveWriter(String name, Object data, Writer writer) {
        JSONObject json = JsonUtils.letJson(data);
        Context context = ApiUtil.createContext(json);
        templateEngine.process(name, context, writer);
    }
}

package com.black.template.freemarker;

import com.black.throwable.IOSException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Data;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author 李桂鹏
 * @create 2023-05-15 14:58
 */
@SuppressWarnings("all") @Data
public class FreemarkerWrapperEngine {

    private static FreemarkerWrapperEngine engine;

    public synchronized static FreemarkerWrapperEngine getInstance() {
        if (engine == null){
            engine = new FreemarkerWrapperEngine();
        }
        return engine;
    }

    private final Configuration configuration;

    private FreemarkerWrapperEngine() {
        configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        this.configuration.setClassForTemplateLoading(FreemarkerWrapperEngine.class, "/");
    }

    public Template getTemplate(String path){
        try {
            return configuration.getTemplate(path);
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    public String processString(String name, Object data){
        StringWriter writer = new StringWriter();
        process(name, data, writer);
        writer.flush();
        return writer.toString();
    }

    public void process(String name, Object data, Writer writer){
        Template template = getTemplate(name);
        try {
            template.process(data, writer);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public void process(String name, Object data, OutputStream outputStream){
        process(name, data, new OutputStreamWriter(outputStream));
    }
}

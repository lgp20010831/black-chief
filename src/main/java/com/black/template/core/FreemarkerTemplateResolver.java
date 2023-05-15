package com.black.template.core;

import com.black.template.freemarker.FreemarkerWrapperEngine;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author 李桂鹏
 * @create 2023-05-15 16:08
 */
@SuppressWarnings("all")
public class FreemarkerTemplateResolver implements TemplateResolver{

    private final FreemarkerWrapperEngine wrapperEngine;

    public FreemarkerTemplateResolver() {
        wrapperEngine = FreemarkerWrapperEngine.getInstance();
    }

    @Override
    public String resolve(String name, Object data) {
        return wrapperEngine.processString(name, data);
    }

    @Override
    public void resolveOutputStream(String name, Object data, OutputStream outputStream) {
        wrapperEngine.process(name, data, outputStream);
    }

    @Override
    public void resolveWriter(String name, Object data, Writer writer) {
        wrapperEngine.process(name, data, writer);
    }
}

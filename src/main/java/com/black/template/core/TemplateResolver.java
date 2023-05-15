package com.black.template.core;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author 李桂鹏
 * @create 2023-05-15 15:59
 */
@SuppressWarnings("all")
public interface TemplateResolver {


    String resolve(String name, Object data);

    void resolveOutputStream(String name, Object data, OutputStream outputStream);

    void resolveWriter(String name, Object data, Writer writer);
}

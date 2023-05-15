package com.black.ftl;

import com.black.template.core.FreemarkerTemplateResolver;
import com.black.throwable.IOSException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-05-15 16:35
 */
@SuppressWarnings("all")
public class FreemarkerNameSpace implements NameSpace{

    private final String templatePath;

    private final FreemarkerTemplateResolver templateResolver;

    public FreemarkerNameSpace(Resource resource){
        try {
            templatePath = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new IOSException(e);
        }

        templateResolver = new FreemarkerTemplateResolver();
    }

    @Override
    public String resolve(Object data) {
        return templateResolver.resolve(templatePath, data);
    }

    @Override
    public String resolveParts(StringJoiner joiner, Object data) {
        return resolve(data);
    }

    @Override
    public String resolvePart(String id, Object data) {
        return resolve(data);
    }
}

package com.black.template;

import com.black.core.util.StringUtils;
import com.black.template.core.TemplateResolver;
import com.black.template.core.ThymeleafTemplateResolver;
import lombok.Getter;
import lombok.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Configuration {

    final List<Location> locations = new ArrayList<>();

    String classPrefix = null;

    String resourcePrefix = null;

    TemplateResolver templateResolver;

    String resolvablePatterns = null;

    public TemplateResolver getTemplateResolver() {
        if (templateResolver == null){
            templateResolver = new ThymeleafTemplateResolver();
        }
        return templateResolver;
    }

    public void setClassPrefix(String classPrefix) {
        this.classPrefix = classPrefix;
    }

    public void setTemplateResolver(TemplateResolver templateResolver) {
        this.templateResolver = templateResolver;
    }

    public void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    public void setResolvablePatterns(String resolvablePatterns) {
        this.resolvablePatterns = resolvablePatterns;
    }

    public Configuration config(@NonNull String templatePath, @NonNull String generatePath,
                                @NonNull String fileName){
        return config(templatePath, generatePath, fileName, false);
    }

    public Configuration config(@NonNull String templatePath, @NonNull String generatePath,
                                @NonNull String fileName, @NonNull boolean isResource){
        Location location = new Location();
        if (isResource){
            if (StringUtils.hasText(resourcePrefix)){
                generatePath = resourcePrefix + "." + generatePath;
            }
        }else {
            if (StringUtils.hasText(classPrefix)){
                generatePath = classPrefix + "." + generatePath;
            }
        }
        location.isResource = isResource;
        location.templatePath = templatePath;
        location.fileName = fileName;
        location.generatePath = generatePath;
        locations.add(location);
        return this;
    }

    @Getter
    public static class Location{
        String fileName;
        boolean isResource;
        String generatePath;
        String templatePath;
    }

}

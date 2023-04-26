package com.black.template;

import com.black.core.util.StringUtils;
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

    TemplateEngine templateEngine;

    String resolvablePatterns = null;

    public TemplateEngine getTemplateEngine() {
        if (templateEngine == null){
            templateEngine = new TemplateEngine();
            final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setOrder(1);
            if (resolvablePatterns != null){
                templateResolver.setResolvablePatterns(Collections.singleton(resolvablePatterns));
            }
            templateResolver.setSuffix(".txt");
            templateResolver.setTemplateMode(TemplateMode.TEXT);
            templateResolver.setCharacterEncoding("UTF-8");
            templateResolver.setCacheable(false);
            templateEngine.addTemplateResolver(templateResolver);
        }
        return templateEngine;
    }

    public void setClassPrefix(String classPrefix) {
        this.classPrefix = classPrefix;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
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

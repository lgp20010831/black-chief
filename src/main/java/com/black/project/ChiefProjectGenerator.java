package com.black.project;


import com.black.template.Configuration;
import com.black.template.TemplateExecutor;
import com.black.template.core.TemplateResolver;

import java.util.*;

public abstract class ChiefProjectGenerator {

    protected final Version version;
    protected TemplateResolver templateResolver;

    protected List<Object> sourceList = new ArrayList<>();

    public ChiefProjectGenerator(Version version) {
        this.version = version;
    }

    public void addSources(Object... objects){
        sourceList.addAll(Arrays.asList(objects));
    }

    public List<Object> getSourceList() {
        return sourceList;
    }

    public Version getVersion() {
        return version;
    }

    public void setTemplateResolver(TemplateResolver templateResolver) {
        this.templateResolver = templateResolver;
    }

    public TemplateResolver getTemplateResolver() {
        return templateResolver;
    }

    protected Map<String, Object> nullSource(){
        return new HashMap<>();
    }

    public void execute(Configuration configuration, Map<String, Object> source){
        if (templateResolver != null){
            configuration.setTemplateResolver(templateResolver);
        }
        TemplateExecutor.getInstance().execute(configuration, source);
    }
}

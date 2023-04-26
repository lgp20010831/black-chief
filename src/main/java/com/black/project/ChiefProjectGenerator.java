package com.black.project;


import com.black.template.Configuration;
import com.black.template.TemplateExecutor;

import java.util.HashMap;
import java.util.Map;

public abstract class ChiefProjectGenerator {

    protected final Version version;

    public ChiefProjectGenerator(Version version) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    protected Map<String, Object> nullSource(){
        return new HashMap<>();
    }

    public void execute(Configuration configuration, Map<String, Object> source){
        TemplateExecutor.getInstance().execute(configuration, source);
    }
}

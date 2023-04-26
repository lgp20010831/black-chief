package com.black.core.template;

import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import lombok.extern.log4j.Log4j2;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Log4j2
@AddHolder
@LoadSort(16)
@IgnorePrint @SuppressWarnings("all")
@LazyLoading(EnableTxtTemplateHolder.class)
public class TxtTemplateHolder implements OpenComponent, EnabledControlRisePotential {

    private TemplateEngine templateEngine;

    private boolean hasDependency;

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

    }

    public boolean isHasDependency(){
        return hasDependency;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        try {
            templateEngine = createTxtEngine();
        }catch (Throwable e){
            if (log.isErrorEnabled()) {
                log.error("load fail txtTemplate, need join dependency");
            }
            hasDependency = false;
            return;
        }
        hasDependency = true;
    }

    private ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setResolvablePatterns(Collections.singleton("autoBuild/*"));
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private TemplateEngine createTxtEngine(){
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(textTemplateResolver());
        return templateEngine;
    }


    public Context createContext(Map<String, Object> source){
        return buildContext(source);
    }

    ChainHashMap<String,Object> buildMap(){
        return new ChainHashMap<>();
    }

    Context buildContext(){
        return buildContext(null);
    }

    Context buildContext(final Map<String, Object> variables){
        Context context = new Context(Locale.CANADA);

        if (variables != null)
            context.setVariables(variables);

        return context;
    }


    public static class ChainHashMap<K,V> extends HashMap<K,V> {

        public ChainHashMap<K, V> chainPut(K key, V value){
            put(key, value);
            return this;
        }

        public ChainHashMap<K, V> chainPutAll(Map<K, V> m){
            putAll(m);
            return this;
        }
    }
}

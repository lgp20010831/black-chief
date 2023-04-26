package com.black.api;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.javassist.DatabaseUniquenessConnectionWrapper;
import com.black.pattern.PremiseManager;
import com.black.utils.NameUtil;
import com.black.utils.ReflectionUtils;
import com.black.vfs.VfsLoader;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

@Getter @Setter
public class Configuration {

    private final VfsLoader loader = new VfsLoader();

    public static String MD_SOURCE_PATH = "autoBuild/api/api3.0.txt";

    public static String HTML_SOURCE_PATH = "autoBuild/api/md-html.txt";

    private ApiType type = ApiType.MD;

    private Connection connection;

    private AliasColumnConvertHandler convertHandler;

    private TemplateEngine templateEngine;

    private boolean wrapperConnection = true;

    private ClassWrapper<? extends RestResponse> responseClass;

    private final Map<String, ClassWrapper<?>> dependencyMap = new HashMap<>();

    private final Set<ClassWrapper<?>> controllerClasses = new HashSet<>();

    private final Map<Class<?>, Set<MethodWrapper>> apiMethods = new HashMap<>();

    private final Map<String, String> globalHeaders = new LinkedHashMap<>();

    private final Set<ApiRequestAndResponseResolver> apiRequestAndResponseResolvers;

    public static final LinkedBlockingQueue<Function<Class<?>, String>> getControllerRemarkFunList = new LinkedBlockingQueue<>();

    public static final LinkedBlockingQueue<Class<? extends ApiRequestAndResponseResolver>> resolverTypes = new LinkedBlockingQueue<>();


    static {
        resolverTypes.add(JdbcApiResolver.class);
        resolverTypes.add(DefaultApiParser.class);
        getControllerRemarkFunList.add(type -> {
            ApiRemark annotation = type.getAnnotation(ApiRemark.class);
            if (annotation == null) return null;
            return StringUtils.hasText(annotation.value()) ? annotation.value() : null;
        });
    }
    public Configuration(){
        apiRequestAndResponseResolvers = new HashSet<>();
        for (Class<? extends ApiRequestAndResponseResolver> resolverType : resolverTypes) {
            Object instance = ReflectionUtils.instance(resolverType);
            if (instance instanceof ConfigurationAware){
                ((ConfigurationAware) instance).setConfiguration(this);
            }
            apiRequestAndResponseResolvers.add((ApiRequestAndResponseResolver) instance);
        }
    }

    public void setConnection(Connection connection) {
        if (isWrapperConnection()){
            this.connection = wrapperConnection0(connection);
        }else {
            this.connection = connection;
        }
    }

    protected Connection wrapperConnection0(Connection connection){
        if(connection instanceof DatabaseUniquenessConnectionWrapper){
            return connection;
        }else {
            return new DatabaseUniquenessConnectionWrapper(connection);
        }
    }

    public Configuration putHeader(String key, String value){
        globalHeaders.put(key, value);
        return this;
    }

    public Configuration putHeaders(Map<String, String> headers){
        if (headers != null){
            globalHeaders.putAll(headers);
        }
        return this;
    }

    public Configuration scanPojo(String packageName){
        Set<Class<?>> classes = loader.load(packageName);
        for (Class<?> aClass : classes) {
            if (BeanUtil.isSolidClass(aClass)){
                add(aClass);
            }
        }
        return this;
    }

    public Configuration scanController(String packageName){
        Set<Class<?>> classes = loader.load(packageName);
        for (Class<?> cla : classes) {
            if (BeanUtil.isSolidClass(cla)){
                addController(cla);
            }
        }
        return this;
    }

    public String getSource() {
        switch (type){
            case MD:
                return MD_SOURCE_PATH;
            case HTML:
                return HTML_SOURCE_PATH;
            default:
                return MD_SOURCE_PATH;
        }
    }


    public TemplateEngine getTemplateEngine() {
        if (templateEngine == null){
            templateEngine = new TemplateEngine();
            final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setOrder(1);
            templateResolver.setResolvablePatterns(Collections.singleton("autoBuild/*"));
            templateResolver.setSuffix(".txt");
            templateResolver.setTemplateMode(TemplateMode.TEXT);
            templateResolver.setCharacterEncoding("UTF-8");
            templateResolver.setCacheable(false);
            templateEngine.addTemplateResolver(templateResolver);
        }
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public ClassWrapper<? extends RestResponse> getResponseClass(Class<?> controllerType) {
        if (responseClass == null){
            Class<? extends RestResponse> classType = AopControllerIntercept.getResponseClassType(controllerType);
            return ClassWrapper.get(classType);
        }
        return responseClass;
    }

    public void setResponseClass(Class<? extends RestResponse> responseClass) {
        this.responseClass = ClassWrapper.get(responseClass);
    }

    public Configuration addController(@NonNull Class<?> controllerClass){
        if (isPremise(controllerClass)){
            ClassWrapper<?> classWrapper = ClassWrapper.get(controllerClass);
            parse(classWrapper);
            controllerClasses.add(classWrapper);
        }
        return this;
    }

    private boolean isPremise(Class<?> controllerClass){
        return PremiseManager.premise(controllerClass);
    }


    private void parse(@NonNull ClassWrapper<?> controllerClass){
        Collection<MethodWrapper> methodByAnnotation = controllerClass.getMethods();

        //找出所有带有 requestmapping 注解的方法
        Set<MethodWrapper> methodWrappers = StreamUtils
                .filterSet(methodByAnnotation, mba -> AnnotationUtils.getAnnotation(mba.getMethod(), RequestMapping.class) != null);

        //保存起来
        apiMethods.put(controllerClass.getPrimordialClass(), methodWrappers);
    }

    public Configuration add(Class<?> dependencyClass){
        String name = NameUtil.getName(dependencyClass);
        dependencyMap.put(name, ClassWrapper.get(dependencyClass));
        return this;
    }

    public Configuration addAll(Collection<Class<?>> dependencyClassCollection){
        for (Class<?> cla :dependencyClassCollection){
            add(cla);
        }
        return this;
    }

    public AliasColumnConvertHandler getConvertHandler() {
        if (convertHandler == null){
            convertHandler = new HumpColumnConvertHandler();
        }
        return convertHandler;
    }
}

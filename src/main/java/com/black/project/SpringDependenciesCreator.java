package com.black.project;

import com.black.ibtais.MybatisDynamicController;
import com.black.core.annotation.Mapper;
import com.black.core.api.ApiUtil;
import com.black.core.mvc.FileUtil;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.util.*;

public class SpringDependenciesCreator {

    public static final String DEFAULT_NAME = "AbstractSpringDependenciesBean";

    private static TemplateEngine templateEngine;

    public static TemplateEngine getTemplateEngine() {
        if (templateEngine == null){
            templateEngine = new TemplateEngine();
            final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setOrder(1);
            templateResolver.setResolvablePatterns(Collections.singleton("fast/*"));
            templateResolver.setSuffix(".txt");
            templateResolver.setTemplateMode(TemplateMode.TEXT);
            templateResolver.setCharacterEncoding("UTF-8");
            templateResolver.setCacheable(false);
            templateEngine.addTemplateResolver(templateResolver);
        }
        return templateEngine;
    }

    public static void execute(@NonNull String scanRange, @NonNull String createPath){
        execute(scanRange, createPath, DEFAULT_NAME);
    }

    public static void execute(@NonNull String scanRange, @NonNull String createPath, Class<?> superClass){
        execute(scanRange, createPath, DEFAULT_NAME, superClass);
    }

    public static void execute(@NonNull String scanRange, @NonNull String createPath, @NonNull String className){
        execute(scanRange, createPath, className, null);
    }

    public static void execute(@NonNull String scanRange, @NonNull String createPath, @NonNull String className, Class<?> superClass){
        String superInfo = "";
        if (superClass != null){
            superInfo = getSuperInfo(superClass);
        }
        VfsScanner scanner = VFS.findVfsScanner();
        Set<Class<?>> classes = scanner.load(scanRange);
        classes = filterClasses(classes);
        List<SpringBean> beans = StreamUtils.mapList(classes, SpringBean::new);
        Set<String> importSet = StreamUtils.mapSet(beans, SpringBean::getImportPath);
        Map<String, Object> env = new HashMap<>();
        env.put("beanSet", beans);
        env.put("importSet", importSet);
        env.put("name", className);
        env.put("package", createPath);
        env.put("superPath", superClass == null ? "" : "import " + superClass.getName() + ";");
        env.put("superInfo", superInfo);
        Context context = ApiUtil.createContext(env);
        TemplateEngine templateEngine = getTemplateEngine();
        String stream = templateEngine.process("fast/super.txt", context);
        String filePath = FileUtil.getFilePath(createPath) + "\\" + className + ".java";
        File file = FileUtil.dropAndcreateFile(filePath);
        FileUtil.writerFile(file, stream);
    }

    public static String getSuperInfo(Class<?> superClass){
        if (superClass.isEnum() || superClass.isInterface()){
            throw new IllegalStateException("can not extends enum or interface");
        }
        String genericString = superClass.toGenericString();
        if (genericString.endsWith(">")){
            String generic = parseGeneric(genericString);
            return generic + " extends " + superClass.getSimpleName() + generic;
        }else {
            return " extends " + superClass.getSimpleName();
        }
    }

    public static String parseGeneric(String genericString){
        return genericString.substring(genericString.indexOf("<"));

    }


    private static Set<Class<?>> filterClasses(Set<Class<?>> classes){
        return StreamUtils.filterSet(classes, clu -> {
            return (AnnotationUtils.getAnnotation(clu, Mapper.class) != null ||
                    AnnotationUtils.getAnnotation(clu, Repository.class) != null ||
                    AnnotationUtils.getAnnotation(clu, Service.class) != null) &&
                    !clu.isAnnotation() && !clu.isAnonymousClass();
        });
    }

    @Getter @Setter
    public static class SpringBean{
        String name;
        String className;
        String importPath;
        public SpringBean(Class<?> type){
            className = type.getSimpleName();
            name = StringUtils.titleLower(className);
            if (!type.isMemberClass()){
                importPath = type.getName();
            }else {
                importPath = type.getName().replace("$", ".");
            }
        }

    }

    public static void main(String[] args) {
        //System.out.println(parseGeneric(MybatisDynamicController.class.toGenericString()));
        execute("com.example", "com.example.project", MybatisDynamicController.class);
    }

}

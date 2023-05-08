package com.black.project;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.mvc.FileUtil;
import com.black.core.util.ClassUtils;
import com.black.template.Configuration;
import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.LinkedHashMap;
import java.util.Map;


public class ProjectInitGenerator extends ChiefProjectGenerator{

    private final Class<?> mainClass;

    private final String packageName;

    public static String CONFIG_NAME = "config";

    public static String UTILS_NAME = "utils";

    public static String MVC_NAME = "mvc";

    private static final IoLog log = LogFactory.getLog4j();

    public static void run(Class<?> mainClass){
        new ProjectInitGenerator(Version.INIT_1_0_FINAL, mainClass).init();
    }

    public ProjectInitGenerator(Version version, Class<?> mainClass) {
        super(version);
        this.mainClass = mainClass;
        packageName = ClassUtils.getPackageName(mainClass);
    }

    public Class<?> getMainClass() {
        return mainClass;
    }

    public String getPackageName() {
        return packageName;
    }

    private String appendPackage(String name){
        return getPackageName() + "." + name;
    }

    public void init(){
        String packageName = getPackageName();

        //创建 config 目录
        String configPath = appendPackage(CONFIG_NAME);
        FileUtil.createClassCatalogue(configPath);
        log.info("create config catalogue finish...");


        //创建 utils 目录
        String utilPath = appendPackage(UTILS_NAME);
        FileUtil.createClassCatalogue(utilPath);
        log.info("create utils catalogue finish...");

        //创建 MVC 目录
        String mvcPath = appendPackage(MVC_NAME);
        FileUtil.createClassCatalogue(mvcPath);
        log.info("create mvc catalogue finish...");

        //写入项目配置类
        writeProjectConfig();

        //写入自动生成代码工具类
        writeAutoCreate();
    }

    private void writeProjectConfig(){
        Configuration configuration = new Configuration();
        configuration.config("init/project_config.txt", appendPackage(CONFIG_NAME), "ProjectConfigurer.java");
        execute(configuration, nullSource());
        log.info("write project config java file finish...");
    }

    private void writeAutoCreate(){
        Configuration configuration = new Configuration();
        configuration.config("init/auto_create.txt", appendPackage(UTILS_NAME), "ProejctAutoCreateCodeUtils.java");
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("mainClassPath", mainClass.getName());
        source.put("projectPath", packageName);
        source.put("mainClassName", mainClass.getSimpleName());
        execute(configuration, source);
        log.info("write auto create utils java file finish...");
    }
}

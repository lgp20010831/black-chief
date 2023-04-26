package com.black.template;

import com.black.core.api.ApiUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TemplateExecutor {


    private TemplateFileResolver fileResolver;

    private static TemplateExecutor executor;

    public TemplateExecutor(){
        fileResolver = new DefaultTemplateFileResolver();
    }

    public static TemplateExecutor getInstance() {
        if (executor == null){
            executor = new TemplateExecutor();
        }
        return executor;
    }
    /**
     *
     * 我想要生成代码, 需要的原材料
     * Map 环境变量
     * 文件地址, 生产位置, 生产文件名
     *
     */

    public void execute(Configuration configuration, Map<String, Object> environment){
        List<Configuration.Location> locations = configuration.getLocations();
        TemplateEngine templateEngine = configuration.getTemplateEngine();
        environment.put("config", configuration);
        Context context = ApiUtil.createContext(environment);
        for (Configuration.Location location : locations) {
            context.setVariable("location", location);
            String buffer = templateEngine.process(location.templatePath, context);

            try {
                fileResolver.resolver(location.fileName, location.generatePath, location.isResource, buffer);
            } catch (IOException e) {
                throw new TemplatesException("写入文件: " + location.fileName + " 发生异常", e);
            }
        }
    }

    public TemplateFileResolver getFileResolver() {
        return fileResolver;
    }

    public void setFileResolver(TemplateFileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

}

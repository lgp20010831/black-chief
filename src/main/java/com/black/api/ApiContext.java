package com.black.api;

import com.black.core.api.ApiUtil;
import com.black.core.mvc.FileUtil;
import com.black.core.util.Av0;
import org.thymeleaf.TemplateEngine;

import java.io.File;
import java.util.List;

public class ApiContext {

    private final Configuration configuration;
    private final HttpApiParser apiParser;


    public ApiContext(Configuration configuration) {
        this.configuration = configuration;
        apiParser = new HttpApiParser(configuration);
    }

    public void write(String filePath){
        String stream = getStream();
        createFile(filePath, stream);
    }

    public String getStream(){
        List<HttpModular> httpMethods = getModular();
        TemplateEngine engine = configuration.getTemplateEngine();
        ApiAssistJqueryGlobalConfig globalConfig = createGlobalConfig();
        return engine.process(configuration.getSource(), ApiUtil.createContext(Av0.of("api", httpMethods, "globalConfig", globalConfig)));
    }

    protected ApiAssistJqueryGlobalConfig createGlobalConfig(){
        return new ApiAssistJqueryGlobalConfig();
    }

    public List<HttpModular> getModular(){
        return apiParser.parseModular();
    }

    protected void createFile(String path, String stream){
        File file = FileUtil.dropAndcreateFile(path);
        FileUtil.writerFile(file, stream);
    }
}

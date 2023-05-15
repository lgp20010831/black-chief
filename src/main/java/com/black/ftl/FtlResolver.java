package com.black.ftl;

import com.black.xml.engine.XmlResolveEngine;
import lombok.Data;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-05-13 15:38
 */
@SuppressWarnings("all") @Data
public class FtlResolver {

    //工作空间缓存
    private final Map<String, NameSpace> nameSpaceCache = new ConcurrentHashMap<>();

    private final XmlResolveEngine xmlResolveEngine;

    private String nullDefaultValue = "";

    private ResolveModel resolveModel = ResolveModel.XML;

    public FtlResolver(){
        xmlResolveEngine = new XmlResolveEngine();
        xmlResolveEngine.addHandler("model", new ModelXmlHandler());
    }

    public void loadPackages(String... dirs){
        for (String dir : dirs) {
            List<Resource> resources = ResourceUtils.getResources(dir, ".ftl");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                nameSpaceCache.computeIfAbsent(filename, fn -> new FtlNameSpace(resource, this));
            }
        }
    }

    private NameSpace createNameSpace(Resource resource){
        switch (resolveModel){
            case XML:
                return new FtlNameSpace(resource, this);
            case FREEMARKER:
                return new FreemarkerNameSpace(resource);
            default:
                throw new IllegalStateException("ill resolve model: " + resolveModel);
        }
    }

    public Map<String, NameSpace> getNameSpaceCache() {
        return nameSpaceCache;
    }

   public NameSpace getNameSpace(String name){
        return nameSpaceCache.get(name);
   }


}

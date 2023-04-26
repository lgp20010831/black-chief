package com.black.core.mybatis.source;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.SimplePattern;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Log4j2
public class MybatisConfigurationHandler {

    private boolean check = true;

    private final IbatisDataSourceGroupConfigurer configurer;

    private final IbatisDynamicallyMultipleDatabasesComponent dynamicallyMultipleDatabasesComponent;

    private final SimplePattern simplePattern;

    public MybatisConfigurationHandler(IbatisDataSourceGroupConfigurer configurer,
                                       IbatisDynamicallyMultipleDatabasesComponent dynamicallyMultipleDatabasesComponent) {
        this.configurer = configurer;
        this.dynamicallyMultipleDatabasesComponent = dynamicallyMultipleDatabasesComponent;
        simplePattern = dynamicallyMultipleDatabasesComponent.getApplication().instanceFactory().getInstance(SimplePattern.class);
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void parseConfig(Configuration configuration){
        if (configuration == null){
            return;
        }

        Collection<String> typeAliasesPackages = dynamicallyMultipleDatabasesComponent.getTypeAliasesPackages();
        TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();

        for (String typeAliasesPackage : typeAliasesPackages) {
            typeAliasRegistry.registerAliases(typeAliasesPackage);
            if (check){
                if (simplePattern != null){
                    Map<String, Class<?>> typeAliases = typeAliasRegistry.getTypeAliases();
                    Set<Class<?>> typeClazzes = simplePattern.loadClasses(typeAliasesPackage);
                    for (Class<?> typeClazz : typeClazzes) {
                        if (!typeAliases.containsValue(typeClazz)){
                            if (log.isInfoEnabled()) {
                                log.info("注册实体类别名: {}", typeClazz.getSimpleName());
                            }
                            typeAliasRegistry.registerAlias(typeClazz.getSimpleName(), typeClazz);
                        }
                    }
                }
            }
        }

        configurer.registerAlias().forEach((a, c) ->{
            configuration.getTypeAliasRegistry().registerAlias(a, c);
        });

        handlerResource(configuration);
    }

    private void handlerResource(Configuration configuration){
        Collection<Resource> mapperResources = dynamicallyMultipleDatabasesComponent.getMapperResources();
        for (Resource mapperResource : mapperResources) {
            try {
                String fileDescription;
                try {
                    fileDescription = getFileDescription(mapperResource.getFile());
                }catch (Exception ex){
                    if (log.isDebugEnabled()) {
                        log.debug("无法获取文件: {}", mapperResource);
                    }
                    fileDescription = getFileDescription(mapperResource.getURL().getPath());
                }

                if (log.isDebugEnabled()) {
                    log.debug("文件描述符:{}", fileDescription);
                }
                //让 mybatis 去解析xml 关联 mapper
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperResource.getInputStream(),
                        configuration, fileDescription, configuration.getSqlFragments());

                xmlMapperBuilder.parse();
                if (fileDescription != null){
                    if (!configuration.isResourceLoaded(fileDescription)){
                        configuration.addLoadedResource(fileDescription);
                    }
                }

            } catch (Throwable e) {
                CentralizedExceptionHandling.handlerException(e);
            }
        }
    }

    public String getFileDescription(File file){
        if (file == null){
            return null;
        }
        return "file [" + (file != null ? file.getAbsolutePath() : "...") + "]";
    }

    public String getFileDescription(String path){
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        return "file [" + path + "]";
    }
}

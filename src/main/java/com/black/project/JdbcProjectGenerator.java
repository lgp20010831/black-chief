package com.black.project;

import com.black.core.sql.action.AbstractCRUDBaseController;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.template.Configuration;
import com.black.template.jdbc.JdbcEnvironmentResolver;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.black.project.BasicConst.*;

@Setter @Getter
public class JdbcProjectGenerator extends ChiefProjectGenerator{

    private boolean supportSwagger = false;

    private boolean supportIllDel = false;

    private String controllerGenPath;

    private String mapperGenPath;

    private String implGenPath;

    private String pojoGenPath;

    Class<?> superControllerType;

    Class<?> superMapperType;

    private DataSourceBuilder dataSourceBuilder;

    private final Map<String, Object> environment = new ConcurrentHashMap<>();

    private String pathPrefix = "";

    public void setPathPrefix(String pathPrefix) {
        pathPrefix = StringUtils.removeIfEndWith(pathPrefix, ".");
        this.pathPrefix = pathPrefix + ".";
    }

    public void setControllerGenPath(String controllerGenPath) {
        this.controllerGenPath = StringUtils.hasText(pathPrefix) ? pathPrefix + controllerGenPath : controllerGenPath;
    }

    public void setImplGenPath(String implGenPath) {
        this.implGenPath = StringUtils.hasText(pathPrefix) ? pathPrefix + implGenPath : implGenPath;;
    }

    public void setPojoGenPath(String pojoGenPath) {
        this.pojoGenPath = StringUtils.hasText(pathPrefix) ? pathPrefix + pojoGenPath : pojoGenPath;;
    }

    public void setMapperGenPath(String mapperGenPath) {
        this.mapperGenPath = StringUtils.hasText(pathPrefix) ? pathPrefix + mapperGenPath : mapperGenPath;;
    }

    public JdbcProjectGenerator(Version version) {
        super(version);
    }

    public void putEnvironment(String key, Object value){
        environment.put(key, value);
    }

    private void check(){
        Assert.notNull(dataSourceBuilder, "dataSourceBuilder is null");
    }

    @Override
    public Version getVersion() {
        return super.getVersion();
    }

    public void writeCodes(String... tableNames){
        check();
        JdbcEnvironmentResolver environmentResolver = new JdbcEnvironmentResolver(dataSourceBuilder);
        try {
            for (String tableName : tableNames) {
                doWriteCode(environmentResolver, tableName);
            }
        }finally {
            environmentResolver.close();
        }
    }

    public void writeCode(String tableName){
        check();
        JdbcEnvironmentResolver environmentResolver = new JdbcEnvironmentResolver(dataSourceBuilder);
        try {
            doWriteCode(environmentResolver, tableName);
        }finally {
            environmentResolver.close();
        }
    }

    private void doWriteCode(JdbcEnvironmentResolver environmentResolver, String tableName){
        Configuration configuration = new Configuration();
        String lowName = StringUtils.ruacnl(tableName);
        String className = StringUtils.titleCase(lowName);
        Map<String, Object> source = environmentResolver.getTemplateSource(tableName);
        source.putAll(environment);
        Connection connection = environmentResolver.getConnection();
        Class<?> controllerType = this.superControllerType;
        if (controllerType == null) {
            controllerType = AbstractCRUDBaseController.class;
        }
        try {
            source.put(CONNECTION_KEY, connection);
            source.put(CLASS_NAME_KEY, className);
            source.put(CONTROLLER_GEN_PATH, controllerGenPath);
            source.put(IMPL_GEN_PATH, implGenPath);
            source.put(MAPPER_GEN_PATH, mapperGenPath);
            source.put(POJO_GEN_PATH, pojoGenPath);
            source.put(SUPER_MAPPER, superMapperType);
            source.put(SUPER_CLASS, controllerType);
            source.put(SUPER_CONTROLLER_GEN_PATH, controllerType.getName());
            source.put(SUPER_CLASS_NAME, controllerType.getSimpleName());
            source.put(SUPER_MAPPER_NAME, superMapperType == null ? null :  superMapperType.getSimpleName());
            source.put(SUPER_MAPPER_GEN_PATH, superMapperType == null ? null : superMapperType.getName());
            List<Object> objects = getSourceList();
            int argIndex = 0;
            for (Object obj : objects) {
                source.put("arg$" + argIndex++, obj);
            }
            Version version = getVersion();
            Supplier<VersionConfigurer> configurerSupplier = version.getConfigurerSupplier();
            VersionConfigurer versionConfigurer = configurerSupplier.get();
            if (versionConfigurer != null){
                versionConfigurer.postConfiguration(configuration, source, this);
            }
            execute(configuration, source);
        }finally {
            SQLUtils.closeConnection(connection);
        }

    }

}

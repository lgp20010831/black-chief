package com.black.project;

import com.black.function.FileFilter;
import com.black.ibtais.IbatisFullParentServlet;
import com.black.ibtais.IbatisWrapperController;
import com.black.core.mvc.FileUtil;
import com.black.core.sql.action.DictDynamicController;
import com.black.core.sql.action.DynamicController;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.table.TableUtils;
import com.black.template.Configuration;
import com.black.template.TemplateExecutor;
import com.black.template.jdbc.JdbcEnvironmentResolver;
import lombok.NonNull;


import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DEMO {

    public static void main(String[] args) {
        //createEnv("ayc", "com.example", "com.example", DynamicController.class, GlobalParentMapping.class);
        creatMybatisMvcEnv("supplier", "com.example", "com.example", "com.example",
                "com.example");
    }

    public static void removeFilesByTableName(String scanPath, String tableName){
        String utableName = StringUtils.titleCase(StringUtils.ruacnl(tableName));
        String pojoName = utableName + ".java";
        String mapperName = utableName + "Mapper.java";
        String implName = utableName + "Impl.java";
        String controllerName = utableName + "Controller.java";
        removeFiles(scanPath, pojoName, mapperName, implName, controllerName);
    }

    public static void removeFiles(String scanPath, String... javaName){
        RemoveFileFilterExecutive executive = new RemoveFileFilterExecutive(new FileFilter() {

            @Override
            public File replaceFile(File file) {
                return FileUtil.castClassFileToIdeaPathJavaFile(file);
            }

            @Override
            public boolean judge(File file) {
                String name = file.getName();
                for (String jn : javaName) {
                    if (name.equals(jn)) {
                        return true;
                    }
                }
                return false;
            }
        });
        executive.removeFiles(scanPath);
    }

    public static void createEnvs( String controllerGenPath,
                                   String mapperGenPath,
                                   Class<? extends DynamicController> superController,
                                   Class<? extends GlobalParentMapping> superMapper,
                                   String... names){
        createEnvs(controllerGenPath, mapperGenPath, superController, superMapper, false, names);
    }

    public static void createEnvs( String controllerGenPath,
                                   String mapperGenPath,
                                   Class<? extends DynamicController> superController,
                                   Class<? extends GlobalParentMapping> superMapper,
                                   boolean full, String... names){
        JdbcEnvironmentResolver environmentResolver = new JdbcEnvironmentResolver(new YmlDataSourceBuilder());
        for (String name : names) {
            Configuration configuration = new Configuration();
            String lowName = StringUtils.ruacnl(name);
            String className = StringUtils.titleCase(lowName);
            configuration.config(full ? "fast/action_full.txt" : "fast/action.txt", controllerGenPath, className + "Controller.java");
            configuration.config("fast/mapper.txt", mapperGenPath, className + "Mapper.java");

            Map<String, Object> source = environmentResolver.getTemplateSource(name);
            source.put("superPath", superController.getName());
            source.put("superName", superController.getSimpleName());
            source.put("superMapperName", superMapper.getSimpleName());
            source.put("superMapperPath", superMapper.getName());
            source.put("mapperPath", mapperGenPath);
            source.put("dict", DictDynamicController.class.isAssignableFrom(superController));
            TemplateExecutor.getInstance().execute(configuration, source);
        }
        environmentResolver.close();
    }

    public static void createEnv(String tableName,
                                 String controllerGenPath,
                                 String mapperGenPath,
                                 Class<? extends DynamicController> superController,
                                 Class<? extends GlobalParentMapping> superMapper){
        Configuration configuration = new Configuration();
        String lowName = StringUtils.ruacnl(tableName);
        String className = StringUtils.titleCase(lowName);
        configuration.config("fast/action.txt", controllerGenPath, className + "Controller.java");
        configuration.config("fast/mapper.txt", mapperGenPath, className + "Mapper.java");
        JdbcEnvironmentResolver environmentResolver;
        Map<String, Object> source = (environmentResolver = new JdbcEnvironmentResolver(new YmlDataSourceBuilder())).getTemplateSource(tableName);
        source.put("superPath", superController.getName());
        source.put("superName", superController.getSimpleName());
        source.put("superMapperName", superMapper.getSimpleName());
        source.put("superMapperPath", superMapper.getName());
        source.put("mapperPath", mapperGenPath);
        TemplateExecutor.getInstance().execute(configuration, source);
        environmentResolver.close();
    }


    public static String UPDATE_FIELD = "updatedAt";

    public static String INSERTED_FIELD = "insertedAt";

    public static Set<String> UPDATE_FIELD_SET = new HashSet<>();

    public static Set<String> INSERTED_FIELD_SET = new HashSet<>();

    static {
        UPDATE_FIELD_SET.add(UPDATE_FIELD);
        INSERTED_FIELD_SET.add(INSERTED_FIELD);
    }

    public static void addUpdateFields(String... ufs){
        for (String uf : ufs) {
            addUpdateField(uf);
        }
    }

    public static void addUpdateField(String uf){
        UPDATE_FIELD_SET.add(uf);
    }

    public static void addInsertFields(String... infs){
        for (String inf : infs) {
            addInsertField(inf);
        }
    }

    public static void addInsertField(String inf){
        INSERTED_FIELD_SET.add(inf);
    }

    public static void creatMybatisMvcEnv(String tableName,
                                          String controllerGenPath,
                                          String mapperGenPath,
                                          String implGenPath,
                                          String pojoGenPath){
        creatMybatisMvcEnv(tableName, controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder(), IbatisWrapperController.class);
    }

    public static void creatMybatisMvcEnv(String tableName,
                                          String controllerGenPath,
                                          String mapperGenPath,
                                          String implGenPath,
                                          String pojoGenPath,
                                          Class<? extends IbatisWrapperController> superController){
        creatMybatisMvcEnv(tableName, controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder(), superController);
    }

    public static void creatMybatisMvcEnv(String tableName,
                                          String controllerGenPath,
                                          String mapperGenPath,
                                          String implGenPath,
                                          String pojoGenPath,
                                          DataSourceBuilder dataSourceBuilder){
        creatMybatisMvcEnv(tableName, controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, dataSourceBuilder, IbatisWrapperController.class);

    }

    public static void creatMybatisMvcEnv(@NonNull String tableName,
                                          @NonNull String controllerGenPath,
                                          @NonNull String mapperGenPath,
                                          @NonNull String implGenPath,
                                          @NonNull String pojoGenPath,
                                          @NonNull DataSourceBuilder dataSourceBuilder,
                                          @NonNull Class<? extends IbatisWrapperController> superController){
        Configuration configuration = new Configuration();
        String lowName = StringUtils.ruacnl(tableName);
        String className = StringUtils.titleCase(lowName);
        configuration.config("fast/mybatis_action.txt", controllerGenPath, className + "Controller.java");
        configuration.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
        configuration.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
        configuration.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        JdbcEnvironmentResolver environmentResolver;
        Map<String, Object> source = (environmentResolver = new JdbcEnvironmentResolver(dataSourceBuilder)).getTemplateSource(tableName);
        source.put("superPath", superController.getName());
        source.put("superName", superController.getSimpleName());
        source.put("mapperPath", mapperGenPath);
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);

        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
        TemplateExecutor.getInstance().execute(configuration, source);
        environmentResolver.close();
    }

    public static void createIbatisFullMvc( @NonNull String controllerGenPath,
                                            @NonNull String mapperGenPath,
                                            @NonNull String implGenPath,
                                            @NonNull String pojoGenPath,
                                            @NonNull String... tableNames){
        createIbatisFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder(), IbatisFullParentServlet.class, tableNames);
    }

    public static void createIbatisFullMvc( @NonNull String controllerGenPath,
                                            @NonNull String mapperGenPath,
                                            @NonNull String implGenPath,
                                            @NonNull String pojoGenPath,
                                            @NonNull DataSourceBuilder dataSourceBuilder,
                                            @NonNull String... tableNames){
        createIbatisFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, dataSourceBuilder, IbatisFullParentServlet.class, tableNames);
    }

    public static void createIbatisFullMvc( @NonNull String controllerGenPath,
                                            @NonNull String mapperGenPath,
                                            @NonNull String implGenPath,
                                            @NonNull String pojoGenPath,
                                            @NonNull Class<? extends IbatisFullParentServlet> superController,
                                            @NonNull String... tableNames){
        createIbatisFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder(), superController, tableNames);
    }

    public static void createIbatisAllTableFullMvc(@NonNull String controllerGenPath,
                                                   @NonNull String mapperGenPath,
                                                   @NonNull String implGenPath,
                                                   @NonNull String pojoGenPath){
        createIbatisAllTableFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder());
    }

    public static void createIbatisAllTableFullMvc(@NonNull String controllerGenPath,
                                                   @NonNull String mapperGenPath,
                                                   @NonNull String implGenPath,
                                                   @NonNull String pojoGenPath,
                                                   @NonNull Class<? extends IbatisFullParentServlet> superController){
        createIbatisAllTableFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, new YmlDataSourceBuilder(), superController);
    }

    public static void createIbatisAllTableFullMvc(@NonNull String controllerGenPath,
                                                   @NonNull String mapperGenPath,
                                                   @NonNull String implGenPath,
                                                   @NonNull String pojoGenPath,
                                                   @NonNull DataSourceBuilder dataSourceBuilder){
        createIbatisAllTableFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, dataSourceBuilder, IbatisFullParentServlet.class);
    }

    public static void createIbatisAllTableFullMvc(@NonNull String controllerGenPath,
                                                   @NonNull String mapperGenPath,
                                                   @NonNull String implGenPath,
                                                   @NonNull String pojoGenPath,
                                                   @NonNull DataSourceBuilder dataSourceBuilder,
                                                   @NonNull Class<? extends IbatisFullParentServlet> superController){
        DataSource dataSource = dataSourceBuilder.getDataSource();
        Connection connection = null;
        String[] tableNames;
        try {
            connection = dataSource.getConnection();
            tableNames = TableUtils.getCurrentTables("demo-project", connection).toArray(new String[0]);
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
        createIbatisFullMvc(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, dataSourceBuilder, superController, tableNames);
    }

    public static void createIbatisFullMvc( @NonNull String controllerGenPath,
                                            @NonNull String mapperGenPath,
                                            @NonNull String implGenPath,
                                            @NonNull String pojoGenPath,
                                            @NonNull DataSourceBuilder dataSourceBuilder,
                                            @NonNull Class<? extends IbatisFullParentServlet> superController,
                                            @NonNull String... tableNames){
        JdbcEnvironmentResolver environmentResolver = new JdbcEnvironmentResolver(dataSourceBuilder);
        try {
            for (String tableName : tableNames) {
                createIbatisFullMvc0(controllerGenPath, mapperGenPath, implGenPath, pojoGenPath, environmentResolver, superController, tableName);
            }
        }finally {
            environmentResolver.close();
        }

    }

    public static boolean onlyCreatePojo = false;

    public static void createIbatisFullMvc0( @NonNull String controllerGenPath,
                                            @NonNull String mapperGenPath,
                                            @NonNull String implGenPath,
                                            @NonNull String pojoGenPath,
                                            @NonNull JdbcEnvironmentResolver environmentResolver,
                                            @NonNull Class<? extends IbatisFullParentServlet> superController,
                                            @NonNull String tableName){
        Configuration configuration = new Configuration();
        String lowName = StringUtils.ruacnl(tableName);
        String className = StringUtils.titleCase(lowName);
        configuration.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            configuration.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            configuration.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            configuration.config("fast/mybatis_action_full.txt", controllerGenPath, className + "Controller.java");
        }

        Map<String, Object> source = environmentResolver.getTemplateSource(tableName);
        source.put("superPath", superController.getName());
        source.put("superName", superController.getSimpleName());
        source.put("mapperPath", mapperGenPath);
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);

        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
        TemplateExecutor.getInstance().execute(configuration, source);
    }
}

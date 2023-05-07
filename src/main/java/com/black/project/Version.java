package com.black.project;

import com.black.ibtais.ObjectIbatisFullController;
import com.black.javassist.DatabaseUniquenessConnectionWrapper;
import com.black.core.sql.action.AbstractCRUDBaseController;
import com.black.core.sql.action.DictDynamicController;
import com.black.core.util.Body;
import com.black.core.util.StringUtils;
import com.black.template.Configuration;

import java.sql.Connection;
import java.util.function.Supplier;

import static com.black.project.BasicConst.*;
import static com.black.project.DEMO.*;
import static com.black.utils.ServiceUtils.getString;

public enum Version {



    CHIEF_1_0(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        config.config("fast/action.txt", controllerGenPath, className + "Controller.java");
        config.config("fast/mapper.txt", mapperGenPath, className + "Mapper.java");
        source.put(DICT, superClass != null && DictDynamicController.class.isAssignableFrom(superClass));
    }),
    CHIEF_1_1(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        config.config("fast/action_full.txt", controllerGenPath, className + "Controller.java");
        config.config("fast/mapper.txt", mapperGenPath, className + "Mapper.java");
        source.put(DICT, superClass != null && DictDynamicController.class.isAssignableFrom(superClass));
    }),
    CHIEF_1_2(() -> (config, source, generator) -> {
        JdbcProjectGenerator jdbcProjectGenerator = (JdbcProjectGenerator) generator;
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Connection connection = (Connection) source.get(CONNECTION_KEY);
        Class<?> superMapper = (Class<?>) source.get(SUPER_MAPPER);
        Boolean createMapper = (Boolean) source.get(CREATE_MAPPER);
        if (createMapper == null){
            createMapper = false;
            source.put(CREATE_MAPPER, createMapper);
        }
        if (superMapper == null){
            DatabaseUniquenessConnectionWrapper connectionWrapper = new DatabaseUniquenessConnectionWrapper(connection);
            String alias = StringUtils.ruacnl(connectionWrapper.getDatabaseName());
            String globalMapperName = StringUtils.titleCase(alias) + "Mapper";
            //创建一个全局 mapper
            Configuration configuration = new Configuration();
            configuration.config("fast/common_mapper.txt", mapperGenPath, globalMapperName + ".java");
            Body body = new Body();
            body.put("mapperName", globalMapperName).put("alias", alias);
            generator.execute(configuration, body);
            source.put(SUPER_MAPPER_NAME,  globalMapperName);
            source.put(SUPER_MAPPER_GEN_PATH, mapperGenPath + "." + globalMapperName);
        }
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        config.config("fast/action_level_1.txt", controllerGenPath, className + "Controller.java");
        if (createMapper)
            config.config("fast/mapper.txt", mapperGenPath, className + "Mapper.java");
        source.put(DICT, DictDynamicController.class.isAssignableFrom(superClass));
        source.put("supportIllDel", jdbcProjectGenerator.isSupportIllDel());
    }),


    MYBATIS_1_0(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        config.config("fast/mybatis_action.txt", controllerGenPath, className + "Controller.java");
        config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
        config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),

    MYBATIS_1_1(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            config.config("fast/mybatis_action_full.txt", controllerGenPath, className + "Controller.java");
        }
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),
    MYBATIS_1_2(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        if (superClass == null || superClass.equals(AbstractCRUDBaseController.class)){
            superClass = ObjectIbatisFullController.class;
            source.put(SUPER_CONTROLLER_GEN_PATH, superClass.getName());
            source.put(SUPER_CLASS_NAME, superClass.getSimpleName());
            source.put(NEED_GENERIC, true);
        }else {
            source.put(NEED_GENERIC, true);
        }
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            config.config("fast/mybatis_action_full_version_2.txt", controllerGenPath, className + "Controller.java");
        }
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),
    MYBATIS_1_3(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        if (superClass == null || superClass.equals(AbstractCRUDBaseController.class)){
            superClass = ObjectIbatisFullController.class;
            source.put(SUPER_CONTROLLER_GEN_PATH, superClass.getName());
            source.put(SUPER_CLASS_NAME, superClass.getSimpleName());
            source.put(NEED_GENERIC, true);
        }else {
            source.put(NEED_GENERIC, true);
        }
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            config.config("fast/mybatis_action_full_version_3.txt", controllerGenPath, className + "Controller.java");
        }
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),
    MYBATIS_1_4(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        if (superClass == null || superClass.equals(AbstractCRUDBaseController.class)){
            superClass = ObjectIbatisFullController.class;
            source.put(SUPER_CONTROLLER_GEN_PATH, superClass.getName());
            source.put(SUPER_CLASS_NAME, superClass.getSimpleName());
            source.put(NEED_GENERIC, true);
        }else {
            source.put(NEED_GENERIC, true);
        }
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            config.config("fast/mybatis_action_full_version_4.txt", controllerGenPath, className + "Controller.java");
        }
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),
    MYBATIS_1_5(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String mapperGenPath = getString(source, MAPPER_GEN_PATH);
        String implGenPath = getString(source, IMPL_GEN_PATH);
        String pojoGenPath = getString(source, POJO_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        Class<?> superClass = (Class<?>) source.get(SUPER_CLASS);
        if (superClass == null || superClass.equals(AbstractCRUDBaseController.class)){
            superClass = ObjectIbatisFullController.class;
            source.put(SUPER_CONTROLLER_GEN_PATH, superClass.getName());
            source.put(SUPER_CLASS_NAME, superClass.getSimpleName());
            source.put(NEED_GENERIC, true);
        }else {
            source.put(NEED_GENERIC, true);
        }
        config.config("fast/mybatis_pojo.txt", pojoGenPath, className + ".java");
        if (!onlyCreatePojo){
            config.config("fast/mybatis_mapper.txt", mapperGenPath, className + "Mapper.java");
            config.config("fast/mybatis_impl.txt", implGenPath, className + "Impl.java");
            config.config("fast/mybatis_action_full_version_5.txt", controllerGenPath, className + "Controller.java");
        }
        source.put("implPath", implGenPath);
        source.put("pojoPath", pojoGenPath);
        source.put("updateFieldName", UPDATE_FIELD);
        source.put("insertFieldName", INSERTED_FIELD);
    }),
    INIT_1_0_FINAL(() -> null),

    SQL_V2(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        config.config("fast/sql_action.txt", controllerGenPath, className + "Controller.java");
    }),
    SQL_V2_CHIEF(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        config.config("fast/sql_action_chief.txt", controllerGenPath, className + "Controller.java");
    }),
    SQL_V2_SWAGGER(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        config.config("fast/sql_action_swagger.txt", controllerGenPath, className + "Controller.java");
    }),
    SQL_V3_CHIEF(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        config.config("fast/sql_action_chief_v2.txt", controllerGenPath, className + "Controller.java");
    }),
    SQL_V3_SWAGGER(() -> (config, source, generator) -> {
        String controllerGenPath = getString(source, CONTROLLER_GEN_PATH);
        String className = getString(source, CLASS_NAME_KEY);
        config.config("fast/sql_action_swagger_v2.txt", controllerGenPath, className + "Controller.java");
    });

    final Supplier<VersionConfigurer> configurerSupplier;


    Version(Supplier<VersionConfigurer> configurerSupplier) {
        this.configurerSupplier = configurerSupplier;
    }

    public Supplier<VersionConfigurer> getConfigurerSupplier() {
        return configurerSupplier;
    }
}

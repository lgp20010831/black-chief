package com.black.project;

import com.black.core.SpringAutoThymeleafApplication;
import com.black.core.sql.code.YmlDataSourceBuilder;

public class ProejctAutoCreateCodeUtils {

    //自动生成 jdbc mvc 三层架构代码, 根据不同版本
    public static void writeJdbc(Version version, String... tableNames){
        JdbcProjectGenerator generator = new JdbcProjectGenerator(version);
        generator.setControllerGenPath("");
        generator.setImplGenPath("");
        generator.setMapperGenPath("");
        generator.setPojoGenPath("");
        generator.setSuperControllerType(null);
        generator.setSuperMapperType(null);
        generator.setDataSourceBuilder(new YmlDataSourceBuilder());
        generator.writeCodes(tableNames);
    }


    //重新进行项目初始化
    public static void reinit(){
        ProjectInitGenerator projectInitGenerator = new ProjectInitGenerator(Version.INIT_1_0_FINAL, SpringAutoThymeleafApplication.class);
        projectInitGenerator.init();
    }

    //自动生成 spring 所以依赖集成器
    public static void createDependencies(){
        SpringDependenciesCreator.execute("", "");
    }

    public static void main(String[] args) {

    }
}

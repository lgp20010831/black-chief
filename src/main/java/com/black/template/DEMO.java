package com.black.template;

import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.util.StringUtils;
import com.black.template.jdbc.JdbcEnvironmentResolver;

import java.util.Map;

public class DEMO {


    public static void main(String[] args) {
        createEntity("appraisal_rules", "com.example");
    }

    static void createMapper(){
        Configuration configuration = new Configuration();
        configuration.config("fast/servlet.txt", "com.example", "AycController.java");
        configuration.config("fast/mapper.txt", "com.example", "AycMapper.java");
        Map<String, Object> source = new JdbcEnvironmentResolver(new YmlDataSourceBuilder()).getTemplateSource("ayc");
        source.put("mapperPath", "com.example");
        TemplateExecutor.getInstance().execute(configuration, source);
    }

    public static void createEntity(String tableName, String model){
        Configuration configuration = new Configuration();
        JdbcEnvironmentResolver environmentResolver = new JdbcEnvironmentResolver(new YmlDataSourceBuilder());
        configuration.config("fast/pojo.txt", model, StringUtils.titleCase(StringUtils.ruacnl(tableName)) + ".java");
        Map<String, Object> source = environmentResolver.getTemplateSource(tableName);
        TemplateExecutor.getInstance().execute(configuration, source);
        environmentResolver.close();
    }
}

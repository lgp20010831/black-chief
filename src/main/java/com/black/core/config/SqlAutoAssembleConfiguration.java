package com.black.core.config;

import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.DefaultDataSourceBuilder;
import com.black.core.util.Utils;
import com.black.sql_v2.Environment;
import com.black.sql_v2.GlobalEnvironment;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.xml.XmlExecutor;
import com.black.xml.XmlSql;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-08 13:15
 */
@Log4j2
@SuppressWarnings("all")
@EnableConfigurationProperties({SqlAutoAssembleConfiguration.SqlIntegrationProperties.class})
public class SqlAutoAssembleConfiguration implements InitializingBean {

    private final SqlIntegrationProperties properties;

    public SqlAutoAssembleConfiguration(SqlIntegrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String driverClass = properties.getDriverClass();
        if (driverClass != null){
            configDefSql(properties);
        }

        XmlSql.opt().setOpenXmlServlet(properties.isOpenServlet());
        List<String> mapperPaths = properties.getMapperPaths();
        if (!Utils.isEmpty(mapperPaths)){
            XmlSql.opt().scanAndParse(mapperPaths.toArray(new String[0]));
        }


        Map<String, SqlAndXmlProperties> sqlMap = properties.getOther();
        if (Utils.isEmpty(sqlMap)){
            return;
        }
        GlobalEnvironment globalEnvironment = GlobalEnvironment.getInstance();
        globalEnvironment.setInsertBatch(properties.insertBatch);
        for (String alias : sqlMap.keySet()) {
            SqlAndXmlProperties sqlAndXmlProperties = sqlMap.get(alias);
            Environment environment = sqlAndXmlProperties.getEnvironment();
            String otherDriverClass = sqlAndXmlProperties.getDriverClass();
            if (otherDriverClass != null){
                DataSourceBuilder dataSourceBuilder = createDataSourceBuilder(sqlAndXmlProperties);
                if (environment != null){
                    environment.setDataSourceBuilder(dataSourceBuilder);
                }
            }
            SqlExecutor sqlExecutor = Sql.lazyOpt(alias);
            sqlExecutor.setEnvironment(environment);
            sqlExecutor.init();
            log.info("init sql executor: {} -- data builder is {}", alias, environment.getDataSourceBuilder());

            XmlExecutor xmlExecutor = XmlSql.opt(alias);
            xmlExecutor.setOpenXmlServlet(sqlAndXmlProperties.isOpenServlet());
            xmlExecutor.scanAndParse(sqlAndXmlProperties.getMapperPaths().toArray(new String[0]));
            log.info("init xml sql executor: {}", alias);
        }
    }

    protected DataSourceBuilder createDataSourceBuilder(SqlAndXmlProperties properties){
        String driverClass = properties.getDriverClass();
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();
        return new DefaultDataSourceBuilder(username, password, driverClass, url);
    }

    private void configDefSql(SqlIntegrationProperties properties){
        String driverClass = properties.getDriverClass();
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();
        DefaultDataSourceBuilder builder = new DefaultDataSourceBuilder(username, password, driverClass, url);
        Sql.configDataSource(Sql.DEFAULT_ALIAS, builder);
    }

    @Data
    @ConfigurationProperties(prefix = "sql")
    public static class SqlIntegrationProperties {

        private int insertBatch = 2000;

        private String driverClass;

        private String username;

        private String password;

        private String url;

        private boolean openServlet = false;

        private List<String> mapperPaths;

        private Map<String, SqlAndXmlProperties> other;

    }

    @Data
    public static class SqlAndXmlProperties{

        private String driverClass;

        private String username;

        private String password;

        private String url;

        private boolean openServlet = false;

        @NestedConfigurationProperty
        private Environment environment = new Environment();

        private List<String> mapperPaths;
    }

}

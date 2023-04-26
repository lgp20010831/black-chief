package com.black.api;


import com.black.core.sql.code.YmlDataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.SQLException;

@RestController
public class APIDOME {


    public static void main(String[] args) throws SQLException {
        createApi();

    }

    public static void createApi() throws SQLException {
        ApiV2Utils.configRequestExclude("is_deleted", "inserted_at", "updated_at", "deleted_at");
        Configuration configuration = new Configuration();
        configuration.setType(ApiType.HTML);
        YmlDataSourceBuilder builder = new YmlDataSourceBuilder();
        DataSource dataSource = builder.getDataSource();
        configuration.setConnection(dataSource.getConnection());
        ApiContext context = new ApiContext(configuration);
        context.write("E:\\ideaSets\\SpringAutoThymeleaf\\docs\\api.html");
        ((HikariDataSource)dataSource).close();
    }

}

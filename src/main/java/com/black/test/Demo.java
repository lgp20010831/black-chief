package com.black.test;

import com.black.api.ApiV2Utils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class Demo {

    public static void main(String[] args) throws SQLException {
        ApiV2Utils.staticColumnValueMap.put("id", () -> UUID.randomUUID().toString());
        DataSource dataSource = new YmlDataSourceBuilder().getDataSource();
        TestApiApplication testApiApplication = new TestApiApplication();
        Configuration configuration = new Configuration(dataSource.getConnection());
        configuration.setComplateUrl("http://localhost:1003");
        configuration.setConcurrency(false);
        testApiApplication.registerListener(new TestListener() {
            @Override
            public Object postRequestParam(Object param, MethodWrapper mw, ClassWrapper<?> cw) {
                if ("select".equals(mw.getName()) || "findSingle".equals(mw.getName())){
                    Map<String, Object> map = (Map<String, Object>) param;
                    map.remove("id");
                }
                return TestListener.super.postRequestParam(param, mw, cw);
            }
        });
        //List<RecordObject> objectList = testApiApplication.test(configuration, DataController.class);
        ((HikariDataSource)dataSource).close();

    }

}

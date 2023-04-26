package com.black.table;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class TABLEDOME {


    public static void main(String[] args) throws SQLException {
        po2();
    }

    static void po2() throws SQLException{
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://10.20.5.52:3306/cy?stringtype=unspecified");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        Connection connection = dataSource.getConnection();
        try {

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet set = metaData.getColumns("cy", null, "views", null);
            ResultSetMetaData data = set.getMetaData();
            while (set.next()) {
                for (int i = 1; i <= data.getColumnCount(); i++) {
                    System.out.println(data.getColumnName(i) + "----" + set.getObject(i));
                }
                System.out.println("----------------");
            }
        }finally {
            connection.close();
            dataSource.close();
        }
    }

    static void po3() throws SQLException{
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/hcy?serverTimezone=UTC&stringtype=unspecified");
        dataSource.setUsername("root");
        dataSource.setPassword("123698745zed");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        Connection connection = dataSource.getConnection();
        try {
            if(connection.getAutoCommit()){
                connection.setAutoCommit(false);
            }
            connection.rollback();
            connection.commit();

        }finally {
            connection.close();
            dataSource.close();
        }
    }

    static void po() throws SQLException {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl("jdbc:postgresql://10.20.255.225:5432/ldb_srm?stringtype=unspecified");
//        dataSource.setUsername("ldb");
//        dataSource.setPassword("LDB@2021");
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        Connection connection = dataSource.getConnection();
//        TableMetadata metadata = TableUtils.getTableMetadata("supplier", connection);
//        ResultSet resultSet = connection.prepareStatement("select * from supplier where is_deleted = false").executeQuery();
//        List<Map<String, Object>> quasiEntities = SQLUtils.processorResultSet(resultSet, metadata);
//        ResultSet resultSet2 = connection.prepareStatement("select * from supplier_licence where is_deleted = false").executeQuery();
//        TableMetadata metadata2 = TableUtils.getTableMetadata("supplier_licence", connection);
//        List<Map<String, Object>> quasiEntities2 = SQLUtils.processorResultSet(resultSet2, metadata2);
//        List<Map<String, Object>> maps = ApplicationUtil.programRunMills(() -> {
//            return integration(quasiEntities, mapFun("id"),
//                    create(quasiEntities2, "licenceList", mapFun("supplier_id"))
//            );
//        });
//        System.out.println(maps);
//        connection.close();
//        dataSource.close();
    }
}

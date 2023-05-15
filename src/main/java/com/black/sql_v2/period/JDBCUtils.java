package com.black.sql_v2.period;


import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StreamUtils;
import com.black.datasource.SpringConnectionWrapper;
import com.black.function.Function;
import com.black.spring.ChiefSpringHodler;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * 用来获取 spring 环境中的 jdbc 成员变量
 * @author 李桂鹏
 * @create 2023-05-13 15:07
 */
@SuppressWarnings("all")
public class JDBCUtils {

    public static final String ALIAS = "JDBC_UTILS_$$";

    public static Connection getConnection(){
        DefaultListableBeanFactory factory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        DataSource dataSource = factory.getBean(DataSource.class);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return new SpringConnectionWrapper(dataSource, connection);
    }


    public static <C> C borrow(Function<Connection, C> function){
        Connection connection = getConnection();
        try {
            return function.apply(connection);
        } catch (Throwable e) {
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
    }

    public static TableMetadata getTableMetadata(String tableName){
        return borrow(connection -> {
            return TableUtils.getTableMetadata(tableName, connection);
        });
    }

    public static Set<String> getTableNames(){
        return borrow(connection -> {
            return TableUtils.getCurrentTables(ALIAS, connection);
        });
    }

    public static List<TableMetadata> getTableMetadatas(){
        Set<String> tableNames = getTableNames();
        return StreamUtils.mapList(tableNames, JDBCUtils::getTableMetadata);
    }

}

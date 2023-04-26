package com.black.core.sql.code.datasource;

import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class ConnectionUtils {


    //检查连接是否可用
    public static boolean checkConnection(Connection connection){
        try {
            return !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            if (log.isWarnEnabled()) {
                log.warn("check connection is closed ?", e);
            }
            return false;
        }
    }

    public static void closeConnection(Connection connection){
        try {
            if (connection != null)
                //关闭连接
                connection.close();
        } catch (SQLException e) {
            //ignore
        }
    }

}

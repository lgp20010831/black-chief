package com.black.db;

import com.black.core.sql.SQLSException;
import com.black.core.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDBConnection implements DBConnection{

    protected boolean close = false;

    protected Connection connection;

    protected Connection getConnection(){
        if (close){
            throw new SQLSException("current connection is closed");
        }
        if(connection == null){
            try {
                connection = getRawConnection();
            } catch (SQLException e) {
                throw new SQLSException(e);
            }
            Assert.notNull(connection, "can not get raw connection");
        }
        return connection;
    }

    protected abstract Connection getRawConnection() throws SQLException;

    @Override
    public Connection getFetchConnection() {
        return getConnection();
    }

    @Override
    public void close() throws SQLException {
        getConnection().close();
    }


}

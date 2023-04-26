package com.black.jdbc.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ObjectSingleResultSetHandler implements ResultSetHandler<Object>{
    @Override
    public Object handlerResultSet(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        return resultSet.getObject(1);
    }
}

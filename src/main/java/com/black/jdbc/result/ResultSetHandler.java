package com.black.jdbc.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface ResultSetHandler<T> {

    /**
     * Processing the result set, this method will
     * perform multiple callbacks, expecting to process
     * one row instead of all datasets
     * @param resultSet result set
     * @param metaData ResultSet Metadata
     * @return Processing generated Java entities
     */
    T handlerResultSet(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException;

}

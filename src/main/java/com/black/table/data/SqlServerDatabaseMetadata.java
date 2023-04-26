package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SqlServerDatabaseMetadata extends AbstractDatabaseMetadata{


    @Override
    public Set<String> getTableNames(Connection connection) {
        try {
            Set<String> ts = new HashSet<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "%", null);
            while (resultSet.next()) {
                ts.add(resultSet.getString("TABLE_NAME"));
            }
            SQLUtils.closeResultSet(resultSet);
            return ts;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    @Override
    public PreparedStatement prepare(String sql, boolean query, boolean allowScroll, Connection connection) throws SQLException {
        PreparedStatement statement;
        if (query){
            if (allowScroll){
                statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            }else {
                statement = connection.prepareStatement(sql);
            }
        }else {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
        return statement;
    }
}

package com.black.core.sql.code;

import java.sql.SQLException;


public interface SetPrepareStatement {


    default void setNull(int i, StatementWrapper statement, int type) throws SQLException {
        statement.getPreparedStatement().setNull(i, type);
    }

    void setValue(StatementWrapper statement) throws SQLException;
}

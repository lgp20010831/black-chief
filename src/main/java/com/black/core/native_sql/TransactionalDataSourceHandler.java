package com.black.core.native_sql;

import java.sql.Connection;

public interface TransactionalDataSourceHandler {

    Connection openConnection();

    void closeConnection(Connection connection);
}

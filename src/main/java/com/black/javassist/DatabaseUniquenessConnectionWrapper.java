package com.black.javassist;

import com.black.core.sql.code.datasource.DynamicConnection;
import com.black.table.TableUtils;
import lombok.NonNull;

import java.sql.Connection;

public class DatabaseUniquenessConnectionWrapper extends DynamicConnection {

    private final Connection connection;

    private final String databaseName;

    public DatabaseUniquenessConnectionWrapper(@NonNull Connection connection) {
        super(null);
        this.connection = connection;
        databaseName = TableUtils.getCurrentDatabaseName(connection);
    }

    @Override
    public Connection loopUp() {
        return connection;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}

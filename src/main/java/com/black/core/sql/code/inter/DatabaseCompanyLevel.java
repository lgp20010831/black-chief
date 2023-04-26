package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DatabaseCompanyLevel {

    List<TableMetadata> getCompanyLevelTable(Configuration configuration, Connection connection) throws SQLException;
}

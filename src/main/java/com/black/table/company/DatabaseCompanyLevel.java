package com.black.table.company;

import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DatabaseCompanyLevel {

    List<TableMetadata> getCompanyLevelTable(TableMetadata masterTable, Connection connection) throws SQLException;
}

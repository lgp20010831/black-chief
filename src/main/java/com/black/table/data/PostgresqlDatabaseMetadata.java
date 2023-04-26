package com.black.table.data;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.lock.LockType;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

@Log4j2
public class PostgresqlDatabaseMetadata extends AbstractDatabaseMetadata{

    public static String GET_CURRENT_TABLES_SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";

    public static String GET_CURRENT_DATABASE_SQL = "select current_database();";


    @Override
    public void lock(Connection connection, String tableName, LockType type) {
        if (log.isDebugEnabled()) {
            log.debug("==> lock table: [" + tableName + "], lock mode is [" + type.getType() + "]");
        }
        String lockSql = "lock table " + tableName + "  in " + type.getType() + " mode";
        SQLUtils.executeSql(lockSql, connection);
    }

    @Override
    public void locks(LockType type, Connection connection, String... tableNames) {
        StringJoiner joiner = new StringJoiner(",");
        for (String tableName : tableNames) {
            joiner.add(tableName);
        }
        String lockSql = "lock table " + joiner.toString() + "  in " + type.getType() + " mode";
        if (log.isInfoEnabled()) {
            log.info("\n{}: [{}]", AnsiOutput.toString(AnsiColor.RED, "==> lock tables "),
                    AnsiOutput.toString(AnsiColor.GREEN, lockSql));
        }
        SQLUtils.executeSql(lockSql, connection);
    }


    @Override
    public String getCurrentDatabase(Connection connection) {
        ResultSet resultSet = null;
        try {

            resultSet = connection.createStatement().executeQuery(GET_CURRENT_DATABASE_SQL);
            String result = null;
            while (resultSet.next()) {
                result = resultSet.getString(1);
            }
            return result;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeResultSet(resultSet);
        }
    }

    @Override
    public Set<String> getTableNames(Connection connection) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_CURRENT_TABLES_SQL);
            Set<String> tableNames = new HashSet<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString(1));
            }
            SQLUtils.closeResultSet(resultSet);
            return tableNames;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeStatement(statement);
        }
    }
}

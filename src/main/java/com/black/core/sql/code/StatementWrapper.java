package com.black.core.sql.code;



import com.black.core.query.Wrapper;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.util.SQLUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementWrapper implements Wrapper<Statement> {

    private final Statement statement;

    public StatementWrapper(Statement statement) {
        this.statement = statement;
    }

    public PreparedStatement getPreparedStatement(){
        if (statement instanceof PreparedStatement){
            return (PreparedStatement) statement;
        }

        throw new IllegalArgumentException("statement is not preparedStatement");
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        getPreparedStatement().clearParameters();
        return get().getGeneratedKeys();
    }

    public boolean execute() throws SQLException {
        return getPreparedStatement().execute();
    }

    public void addBatch() throws SQLException {
        getPreparedStatement().addBatch();
    }

    public void clearBatch() throws SQLException {
        getPreparedStatement().clearBatch();
    }

    public ResultSet getGKey() throws SQLException {
        return getPreparedStatement().getGeneratedKeys();
    }

    public boolean executeAndExecuteBatch(boolean emtryBatch, int batch, ExecuteBody body) throws SQLException {
        if (emtryBatch){
            int i = executeUpdate();
            body.setUpdateCount(i);
        }else {
            executeBatch();
            body.setUpdateCount(batch);
        }
        return true;
    }

    public boolean executeBatch() throws SQLException {
        getPreparedStatement().executeBatch();
        return true;
    }

    public ResultSet executeQuery() throws SQLException {
        return getPreparedStatement().executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return getPreparedStatement().executeUpdate();
    }

    public void close(){
        SQLUtils.closeStatement(statement);
    }

    @Override
    public Statement get() {
        return statement;
    }
}

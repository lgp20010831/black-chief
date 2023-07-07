package com.black.database.calcite;

import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author 李桂鹏
 * @create 2023-06-26 13:52
 */
@SuppressWarnings("all")
public class SchemaDataSource implements DataSource {

    private final String schemaName;

    private Properties properties;

    private String url = "jdbc:calcite:";

    public SchemaDataSource(String schemaName) {
        this.schemaName = schemaName;
        properties = new Properties();
        properties.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        properties.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    protected void loadSchema(CalciteConnection calciteConnection){
        Map<String, MemorySchema> schemaMap = SchemaManager.getSchemaMap();
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        schemaMap.forEach((name, schema) -> {
            rootSchema.add(name, schema);
        });
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:calcite:", properties);
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        calciteConnection.setSchema(schemaName);
        loadSchema(calciteConnection);
        return calciteConnection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("unwrap");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("isWrapperFor");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("PrintWriter");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("getLoginTimeout");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("getParentLogger");
    }
}

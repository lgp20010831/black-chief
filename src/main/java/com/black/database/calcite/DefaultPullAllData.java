package com.black.database.calcite;

import com.black.core.sql.code.util.SQLUtils;
import com.black.sql.SqlWriter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-26 15:30
 */
@SuppressWarnings("all")
public class DefaultPullAllData implements PullTableData{
    @Override
    public List<Map<String, Object>> pull(Connection connection, String tableName) throws SQLException {
        String sql = SqlWriter.select(tableName).flush().toString();
        return SQLUtils.runJavaSelect(sql, connection, null);
    }
}

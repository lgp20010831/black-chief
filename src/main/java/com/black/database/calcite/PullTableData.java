package com.black.database.calcite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-26 15:29
 */
@SuppressWarnings("all")
public interface PullTableData {


    List<Map<String, Object>> pull(Connection connection, String tableName) throws SQLException;

}

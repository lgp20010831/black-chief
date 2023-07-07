package com.black.database.calcite;

import com.black.core.sql.SQLSException;
import com.black.function.Callable;
import com.black.function.Callback;
import com.black.sql_v2.Environment;
import com.black.sql_v2.SqlExecutor;
import com.black.table.ColumnMetadata;
import com.black.table.DefaultColumnMetadata;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.template.jdbc.JavaColumnMetadata;
import com.black.utils.IdUtils;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-26 13:38
 */
@SuppressWarnings("all")
public class CalciteUtils {

    public static MemoryTable castToTable(TableMetadata metadata){
        BasicMemoryTable memoryTable = new BasicMemoryTable(metadata.getTableName());
        Collection<ColumnMetadata> columnMetadatas = metadata.getColumnMetadatas();
        for (ColumnMetadata columnMetadata : columnMetadatas) {
            MemoryColumn memoryColumn = castToColumn(columnMetadata);
            memoryTable.addColumns(memoryColumn);
        }
        return memoryTable;
    }

    public static MemoryColumn castToColumn(ColumnMetadata columnMetadata){
        JavaColumnMetadata javaColumnMetadata = new JavaColumnMetadata((DefaultColumnMetadata) columnMetadata);
        BasicMemoryColumn memoryColumn = new BasicMemoryColumn(columnMetadata.getName(), javaColumnMetadata.getJavaClass());
        memoryColumn.setRemark(javaColumnMetadata.getRemarks());
        return memoryColumn;
    }

    public static <C> C pullCall(Callable<C> callable, SqlExecutor executor,
                                 PullTableData pullTableData, String... pullTableNames){
        if (pullTableData == null){
            pullTableData = new DefaultPullAllData();
        }
        Connection connection = executor.getConnection();
        MemorySchema schema = pullSchema(connection, pullTableNames);
        Map<String, Table> tableMap = schema.getTableMap();
        for (String tableName : tableMap.keySet()) {
            Table table = tableMap.get(tableName);
            MemoryTable memoryTable = (MemoryTable) table;
            try {
                List<Map<String, Object>> maps = pullTableData.pull(connection, tableName);
                memoryTable.addAllData(maps);
            } catch (SQLException e) {
                throw new SQLSException("An error occurred while pulling data: " + tableName, e);
            }
        }
        SchemaManager.registerSchema(schema);
        Environment environment = executor.getEnvironment();
        environment.setSchemaLocal(schema.getName());
        environment.registerMemoryTables(pullTableNames);
        try {
            return callable.call();
        } catch (Throwable e) {
            throw new SQLSException(e);
        }finally {
            SchemaManager.removeSchame(schema.getName());
            environment.clearMemoryTables();
            environment.clearMemoryTables();
        }
    }

    public static MemorySchema pullSchema(Connection connection, String... pullTableNames){
        String schemaName = IdUtils.createShort8Id();
        MemorySchema memorySchema = new MemorySchema(schemaName);
        for (String pullTableName : pullTableNames) {
            TableMetadata metadata = TableUtils.getTableMetadata(pullTableName, connection);
            MemoryTable memoryTable = castToTable(metadata);
            memorySchema.addTables(memoryTable);
        }
        return memorySchema;
    }
}

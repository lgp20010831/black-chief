package com.black.database.calcite;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-06-26 14:03
 */
@SuppressWarnings("all")
public class MemorySchema extends AbstractSchema {

    static {

    }

    private final String name;

    public MemorySchema(String name) {
        this.name = name;
    }

    public static MemorySchema create(String name, MemoryTable... tables){
        MemorySchema memorySchema = new MemorySchema(name);
        memorySchema.addTables(tables);
        return memorySchema;
    }

    private final Map<String, Table> tableMap = new ConcurrentHashMap<>();

    public String getName() {
        return name;
    }

    public void addTables(MemoryTable... tables){
        for (MemoryTable table : tables) {
            tableMap.put(table.getTableName(), table);
        }
    }

    @Override
    protected Map<String, Table> getTableMap() {
        return tableMap;
    }
}

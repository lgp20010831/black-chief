package com.black.database.calcite;

import lombok.NonNull;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.tools.Frameworks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-06-26 13:49
 */
@SuppressWarnings("all")
public class SchemaManager {

    private static final Map<String, MemorySchema> schemaMap = new ConcurrentHashMap<>();

    public static void registerSchema(@NonNull MemorySchema schema){
        schemaMap.put(schema.getName(), schema);
    }

    public static Schema getSchema(String name){
        return schemaMap.get(name);
    }

    public static Map<String, MemorySchema> getSchemaMap() {
        return schemaMap;
    }

    public static void removeSchame(String name){
        schemaMap.remove(name);
    }
}

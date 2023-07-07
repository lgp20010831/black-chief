package com.black.database.calcite;

import org.apache.calcite.schema.Table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 李桂鹏
 * @create 2023-06-26 11:54
 */
@SuppressWarnings("all")
public interface MemoryTable extends Table {

    String getTableName();

    List<Map<String, Object>> getDataList();

    void addData(Map<String, Object> data);

    void addData(Map<String, Object>... datas);

    void addAllData(Collection<Map<String, Object>> datas);

    LinkedBlockingQueue<MemoryColumn> getColumns();

    void addColumns(MemoryColumn... columns);

    void addAllColumns(Collection<MemoryColumn> columnCollection);
}

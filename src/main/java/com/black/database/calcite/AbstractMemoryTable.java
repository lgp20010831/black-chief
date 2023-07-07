package com.black.database.calcite;


import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeUtil;
import org.apache.calcite.util.Pair;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 李桂鹏
 * @create 2023-06-26 13:32
 */
@SuppressWarnings("all")
public abstract class AbstractMemoryTable extends AbstractTable implements MemoryTable, ScannableTable {

    protected final String tableName;

    protected final List<Map<String, Object>> dataList = new ArrayList<>();

    protected final LinkedBlockingQueue<MemoryColumn> columns = new LinkedBlockingQueue<>();

    public AbstractMemoryTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public LinkedBlockingQueue<MemoryColumn> getColumns() {
        return columns;
    }

    public void addColumns(MemoryColumn... columns){
        this.columns.addAll(Arrays.asList(columns));
    }

    public void addAllColumns(Collection<MemoryColumn> columnCollection){
        columns.addAll(columnCollection);
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void addData(Map<String, Object> data){
        if (data != null){
            dataList.add(data);
        }
    }

    public void addData(Map<String, Object>... datas){
        if (datas != null){
            dataList.addAll(Arrays.asList(datas));
        }
    }

    public void addAllData(Collection<Map<String, Object>> datas){
        if (datas != null){
            dataList.addAll(datas);
        }
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        JavaTypeFactory typeFactory = (JavaTypeFactory) relDataTypeFactory;
        List<String> names = new ArrayList<>();
        List<RelDataType> types = new ArrayList<>();
        for(MemoryColumn col : getColumns()){
            names.add(col.getName());
            RelDataType relDataType = typeFactory.createJavaType(col.getJavaType());
            relDataType = SqlTypeUtil.addCharsetAndCollation(relDataType, typeFactory);
            types.add(relDataType);
        }
        return typeFactory.createStructType(Pair.zip(names,types));
    }

    @Override
    public Enumerable<Object[]> scan(DataContext dataContext) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new MapMemoryEnumerator(dataList);
            }
        };
    }
}

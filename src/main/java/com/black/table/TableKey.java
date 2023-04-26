package com.black.table;

public interface TableKey {

    //所属表信息
    TableMetadata getRawTableMetadata();

    //获取该键值对应的列
    ColumnMetadata getRawColumnMetadata();

    //获取列名
    String getName();

}

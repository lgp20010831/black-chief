package com.black.table;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TableMetadata {

    //获取表名
    String getTableName();

    //获取所有的字段
    Collection<ColumnMetadata> getColumnMetadatas();

    //根据列名获取列东西
    ColumnMetadata getColumnMetadata(String name);

    //返回当前表存在多少个主键
    int primaryKeysSize();

    //返回当前表存在多少个外键
    int foreignKeysSize();

    List<String> getColumnNameList();

    //获取列名 set 集合
    Set<String> getColumnNameSet();

    //跟据字段名查询主键, 不存在返回空
    PrimaryKey getPrimaryKey(String name);

    //跟据字段名查询外键, 不存在返回空
    ForeignKey getForeignKey(String name);

    //返回第一个主键, 如果没有主键, 则返回空
    PrimaryKey firstPrimaryKey();

    //返回第一个外键, 如果没有外键, 则返回空
    ForeignKey firstForeignKey();

    ForeignKey getForeignByPrimaryName(String name);

    ForeignKey getForeignByPrimaryNameAndTableName(String name, String tableName);



    //获取所有的联合主键
    Collection<PrimaryKey> getPrimaryKeys();

    //获取所有的外键
    Collection<ForeignKey> getForeignKeys();

    //返回 key = 字段名， value = 主键的 map结构
    Map<String, PrimaryKey> getPrimaryKeyMap();

    //返回 key = 字段名， value = 外键的 map结构
    Map<String, ForeignKey> getForeignKeyMap();

    //返回 key = 字段名， value = 字段的 map结构
    Map<String, ColumnMetadata> getColumnMetadataMap();

    //设置备注信息
    void setRemark(String remark);

    //返回备注信息
    String getRemark();

    void findSubset(Connection connection);

    Map<String, TableMetadata> getSubsetMetadata();

    Collection<TableMetadata> getSubsetMetadataList();

    TableMetadata getSubMetadata(String name);
}

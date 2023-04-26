package com.black.table;

import com.black.table.company.CompanyLevelDriver;
import lombok.NonNull;

import java.sql.Connection;
import java.util.*;

public abstract class AbstractTableMetadata implements TableMetadata{

    protected final String tableName;

    protected final Map<String, ColumnMetadata> columnMetadatas = new LinkedHashMap<>();

    protected final Map<String, PrimaryKey> primaryKeyMap = new LinkedHashMap<>();

    protected final Map<String, ForeignKey> foreignKeyMap = new LinkedHashMap<>();

    protected final Map<String, TableMetadata> subsetMetadata = new LinkedHashMap<>();

    protected String remark;

    private boolean findSubset = false;

    //提供简单构造, 解决外键的循环查找
    public AbstractTableMetadata(@NonNull String tableName){
        this.tableName = tableName;
    }

    public AbstractTableMetadata(@NonNull String tableName,
                                 @NonNull Collection<ColumnMetadata> columnMetadataList,
                                 Collection<PrimaryKey> primaryKeys,
                                 Collection<ForeignKey> foreignKeys) {
        this.tableName = tableName;
        for (ColumnMetadata columnMetadata : columnMetadataList) {
            columnMetadatas.put(columnMetadata.getName(), columnMetadata);
        }
        if (primaryKeys != null){
            for (PrimaryKey primaryKey : primaryKeys) {
                primaryKeyMap.put(primaryKey.getName(), primaryKey);
            }
        }

        if (foreignKeys != null){
            resetForeignKey(foreignKeys);
        }
    }

    public void registerColumnMetadata(@NonNull Collection<ColumnMetadata> columnMetadataList){
        for (ColumnMetadata columnMetadata : columnMetadataList) {
            columnMetadatas.put(columnMetadata.getName(), columnMetadata);
        }
    }

    public void resetPrimaryKey(Collection<PrimaryKey> primaryKeys){
        if (primaryKeys != null){
            for (PrimaryKey primaryKey : primaryKeys) {
                primaryKeyMap.put(primaryKey.getName(), primaryKey);
            }
        }
    }

    public void resetForeignKey(Collection<ForeignKey> foreignKeys){
        if (foreignKeys != null){
            for (ForeignKey foreignKey : foreignKeys) {
                foreignKeyMap.put(foreignKey.getName(), foreignKey);
            }

            for (ForeignKey foreignKey : foreignKeys) {
                TableMetadata tableMetadata = foreignKey.getMappingPrimaryKey().getRawTableMetadata();
            }

        }
    }

    @Override
    public TableMetadata getSubMetadata(String name) {
        return subsetMetadata.get(name);
    }

    public Map<String, TableMetadata> getSubsetMetadata() {
        return subsetMetadata;
    }

    @Override
    public Collection<TableMetadata> getSubsetMetadataList() {
        return subsetMetadata.values();
    }

    @Override
    public void findSubset(Connection connection) {
        if (!findSubset){
            findSubset = true;
            subsetMetadata.clear();
            List<TableMetadata> metadataList = CompanyLevelDriver.getCompanyLevelTable(this, connection);
            for (TableMetadata tableMetadata : metadataList) {
                subsetMetadata.put(tableMetadata.getTableName(), tableMetadata);
            }
        }
    }

    @Override
    public ColumnMetadata getColumnMetadata(String name) {
        return columnMetadatas.get(name);
    }

    @Override
    public Set<String> getColumnNameSet() {
        return columnMetadatas.keySet();
    }

    @Override
    public Collection<ColumnMetadata> getColumnMetadatas() {
        return columnMetadatas.values();
    }

    @Override
    public int primaryKeysSize() {
        return primaryKeyMap.size();
    }

    @Override
    public List<String> getColumnNameList() {
        Set<String> columnNameSet = getColumnNameSet();
        return new ArrayList<>(columnNameSet);
    }

    @Override
    public int foreignKeysSize() {
        return foreignKeyMap.size();
    }

    @Override
    public PrimaryKey getPrimaryKey(String name) {
        return primaryKeyMap.get(name);
    }

    @Override
    public ForeignKey getForeignKey(String name) {
        return foreignKeyMap.get(name);
    }

    @Override
    public PrimaryKey firstPrimaryKey() {
        Iterator<PrimaryKey> keyIterator = getPrimaryKeys().iterator();
        PrimaryKey key = null;
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
        }
        return key;
    }

    @Override
    public ForeignKey firstForeignKey() {
        Iterator<ForeignKey> keyIterator = getForeignKeys().iterator();
        ForeignKey key = null;
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
        }
        return key;
    }

    @Override
    public ForeignKey getForeignByPrimaryName(String name) {
        for (ForeignKey foreignKey : getForeignKeys()) {
            if (name.equals(foreignKey.getMappingPrimaryKey().getName())){
                return foreignKey;
            }
        }
        return null;
    }

    @Override
    public ForeignKey getForeignByPrimaryNameAndTableName(String name, String tableName) {
        for (ForeignKey foreignKey : getForeignKeys()) {
            PrimaryKey mappingPrimaryKey = foreignKey.getMappingPrimaryKey();
            if (name.equals(mappingPrimaryKey.getName()) &&
                    mappingPrimaryKey.getRawTableMetadata().getTableName().equals(tableName)){
                return foreignKey;
            }
        }
        return null;
    }

    @Override
    public Collection<PrimaryKey> getPrimaryKeys() {
        return primaryKeyMap.values();
    }

    @Override
    public Collection<ForeignKey> getForeignKeys() {
        return foreignKeyMap.values();
    }

    @Override
    public Map<String, PrimaryKey> getPrimaryKeyMap() {
        return primaryKeyMap;
    }

    @Override
    public Map<String, ForeignKey> getForeignKeyMap() {
        return foreignKeyMap;
    }

    @Override
    public Map<String, ColumnMetadata> getColumnMetadataMap() {
        return columnMetadatas;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public String toString() {
        return "TableMetadata{" +
                "tableName='" + tableName + '\'' +
                ", columnMetadatas=" + columnMetadatas +
                ", primaryKeyMap=" + primaryKeyMap.keySet() +
                ", foreignKeyMap=" + foreignKeyMap.keySet() +
                ", remark='" + remark + '\'' +
                '}';
    }
}

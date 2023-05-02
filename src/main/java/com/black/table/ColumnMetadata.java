package com.black.table;

public interface ColumnMetadata {

    String getName();

    int getSize();

    boolean autoIncrement();

    String getRemarks();

    void setRemarks(String remarks);

    int getType();

    String getTypeName();

    TableMetadata getRawTableMetadata();
}

package com.black.table;

import lombok.NonNull;

public abstract class AbstractColumnMetadata implements ColumnMetadata{

    protected final String name;

    protected final int size;

    protected String remarks;

    protected final String typeName;

    protected final int type;

    protected TableMetadata metadata;

    public AbstractColumnMetadata(String name, int size, String typeName, int type) {
        this.name = name;
        this.size = size;
        this.typeName = typeName;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getRemarks() {
        return remarks;
    }

    @Override
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public TableMetadata getRawTableMetadata() {
        return metadata;
    }

    public void setMetadata(@NonNull TableMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ColumnMetadata{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", remarks='" + remarks + '\'' +
                ", typeName='" + typeName + '\'' +
                ", type=" + type +
                '}';
    }
}

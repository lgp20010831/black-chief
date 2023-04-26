package com.black.table;

public abstract class AbstractTableKey implements TableKey{

    protected final ColumnMetadata columnMetadata;

    public AbstractTableKey(ColumnMetadata columnMetadata) {
        if (columnMetadata == null){
            throw new IllegalArgumentException("columnMetadata must be not null");
        }
        this.columnMetadata = columnMetadata;
    }

    @Override
    public TableMetadata getRawTableMetadata() {
        return columnMetadata.getRawTableMetadata();
    }

    @Override
    public ColumnMetadata getRawColumnMetadata() {
        return columnMetadata;
    }

    @Override
    public String getName() {
        return columnMetadata.getName();
    }
}

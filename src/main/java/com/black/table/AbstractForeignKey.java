package com.black.table;

public abstract class AbstractForeignKey extends AbstractTableKey implements ForeignKey{

    protected final PrimaryKey mappingPrimaryKey;

    public AbstractForeignKey(ColumnMetadata columnMetadata, PrimaryKey mappingPrimaryKey) {
        super(columnMetadata);
        if (mappingPrimaryKey == null){
            throw new IllegalArgumentException("primaryKey must be not null, current table: " + columnMetadata.getRawTableMetadata().getTableName() + "" +
                    " current foreign column: " + columnMetadata.getName());
        }
        this.mappingPrimaryKey = mappingPrimaryKey;
    }

    @Override
    public PrimaryKey getMappingPrimaryKey() {
        return mappingPrimaryKey;
    }
}

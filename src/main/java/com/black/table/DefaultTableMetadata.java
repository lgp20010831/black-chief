package com.black.table;

import lombok.NonNull;

import java.util.Collection;

public class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      DefaultTableMetadata extends AbstractTableMetadata{

    public DefaultTableMetadata(String name){
        super(name);
    }

    public DefaultTableMetadata(@NonNull String tableName,
                                @NonNull Collection<ColumnMetadata> columnMetadataList,
                                Collection<PrimaryKey> primaryKeys,
                                Collection<ForeignKey> foreignKeys) {
        super(tableName, columnMetadataList, primaryKeys, foreignKeys);
    }
}

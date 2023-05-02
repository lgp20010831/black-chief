package com.black.table;

public interface PrimaryKey extends TableKey{

    default boolean autoIncrement(){
        return getRawColumnMetadata().autoIncrement();
    }
}

package com.black.api.handler;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.table.TableUtils;

import java.sql.Connection;

public class TableMetadataBuilder implements MetadataBuilder {

    private final AliasColumnConvertHandler columnConvertHandler;

    public TableMetadataBuilder() {
        columnConvertHandler = new HumpColumnConvertHandler();
    }

    @Override
    public Object buildMatedata(String plane, Connection connection) {
        String name = columnConvertHandler.convertColumn(plane);
        if (connection == null){
            return null;
        }
        return TableUtils.getTableMetadata(name, connection);
    }
}

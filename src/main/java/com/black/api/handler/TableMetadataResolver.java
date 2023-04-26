package com.black.api.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.table.ColumnMetadata;
import com.black.table.TableMetadata;
import com.black.template.jdbc.JdbcType;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.black.api.ApiV2Utils.*;

public class TableMetadataResolver implements MetadataResolver {
    @Override
    public boolean support(Object metadata) {
        return metadata instanceof TableMetadata;
    }

    @Override
    public void resolve(Object metadata, JSONObject sonJson, AliasColumnConvertHandler handler, boolean request) {
        TableMetadata tableMetadata = (TableMetadata) metadata;
        for (ColumnMetadata columnMetadata : tableMetadata.getColumnMetadatas()) {
            if (request && requestExcludes.contains(columnMetadata.getName())){
                continue;
            }
            JdbcType jdbcType = JdbcType.getByName(columnMetadata.getTypeName().toUpperCase());
            Class<?> javaClass = jdbcType.getJavaClass();
            String alias = handler.convertAlias(columnMetadata.getName());
            if (remarkJoin){
                wriedRemark(sonJson, alias, javaClass, columnMetadata.getRemarks(), jdbcType, request);
            }else {
                if (javaClass != null && javaClass.equals(Boolean.class)){
                    writeBoolean(sonJson, alias);
                }else
                if (javaClass != null && Number.class.isAssignableFrom(javaClass)){
                    wriedInt(sonJson, alias);
                }else if ((Date.class.equals(javaClass) || Time.class.equals(javaClass) || Timestamp.class.equals(javaClass) || LocalDateTime.class.equals(javaClass))){
                    wriedDate(sonJson, alias);
                }else {
                    wriedString(sonJson, handler.convertAlias(columnMetadata.getName()), javaClass);
                }
            }
        }
    }
}

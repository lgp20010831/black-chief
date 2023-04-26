package com.black.core.sql.run;

import com.black.core.json.Alias;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.util.StringUtils;
import com.black.table.*;

import java.util.Map;
import java.util.StringJoiner;

public class BaseRunner implements RunSupport{
    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(Alias.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        Class<?> returnType = mw.getReturnType();
        Alias annotation = mw.getAnnotation(Alias.class);
        String value = annotation.value();
        if ("getAlias".equals(value) && returnType.equals(String.class)){
            return configuration.getDataSourceAlias();
        }else if ("showTableInfo".equals(value) && returnType.equals(String.class) && mw.getParameterCount() == 1){
            return getTableInfo(args[0], configuration);
        }
        return null;
    }

    private String getTableInfo(Object tableName, GlobalSQLConfiguration configuration){
        if (tableName == null){
            return "NULL TABLENAME?";
        }

        return ConnectionManagement.employConnection(configuration.getDataSourceAlias(), connection -> {
            TableMetadata metadata = TableUtils.getTableMetadata(tableName.toString(), connection);
            if (metadata == null){
                return "unknown table: " + tableName;
            }
            String info = "表名: " + metadata.getTableName() + "; 备注: " + metadata.getRemark() + "\n";
            StringJoiner joiner = new StringJoiner(";\n");
            Map<String, PrimaryKey> primaryKeyMap = metadata.getPrimaryKeyMap();
            Map<String, ForeignKey> foreignKeyMap = metadata.getForeignKeyMap();
            for (ColumnMetadata columnMetadata : metadata.getColumnMetadatas()) {
                String name = columnMetadata.getName();
                ForeignKey foreignKey = foreignKeyMap.get(name);
                String fkInfo = "无";
                if (foreignKey != null){
                    PrimaryKey mappingPrimaryKey = foreignKey.getMappingPrimaryKey();
                    fkInfo = StringUtils.letString("UNKNOWN", "外键关联表名:",
                            mappingPrimaryKey.getRawTableMetadata().getTableName(), ";外键关联外表主键:", mappingPrimaryKey.getName());
                }
                joiner.add(StringUtils.letString("UNKNOWN","字段名称:", name,
                        ";类型:", columnMetadata.getTypeName(), ";长度:", columnMetadata.getSize(),
                        ";备注:", columnMetadata.getRemarks(), ";是否为主键:", primaryKeyMap.containsKey(name),
                        "; 是否为外键:", foreignKey != null, ";外键关联情况:", fkInfo));
            }
            return info + joiner;
        });
    }
}

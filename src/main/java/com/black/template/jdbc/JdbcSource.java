package com.black.template.jdbc;

import com.black.core.util.Av0;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class JdbcSource {
    String tableName;

    String className;

    String lowName;

    TableMetadata tableMetadata;

    String remark;

    List<String> primaryKeys = new ArrayList<>();

    public JdbcSource(TableMetadata metadata){
        tableMetadata = metadata;
        tableName = metadata.getTableName();
        String remark = tableMetadata.getRemark();
        this.remark = StringUtils.hasText(remark) ? remark : getTableName() + "表";
        className = StringUtils.titleCase(lowName = Av0.ruacnl(tableName));
        primaryKeys.addAll(StreamUtils.mapList(metadata.getPrimaryKeys(), PrimaryKey::getName));
    }
}

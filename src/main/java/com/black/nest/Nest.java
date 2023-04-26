package com.black.nest;

import com.black.core.json.Trust;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Connection;

@Data @NoArgsConstructor @AllArgsConstructor @Trust
public class Nest {

    //id
    private String id;

    //父表名称
    private String parentName;

    //子表名称
    private String sonName;

    //一对多 ? 1:true: 0:false
    private int oneMany = 1;

    //父表关联字段
    private String parentKey;

    //子表关联字段
    private String sonKey;

    //拼接 sql
    private String applySql;

    //参数后缀
    private String suffix;

    //创建时间
    private String createdAt;

    public boolean wasOneMany(){
        return oneMany == 1;
    }

    public TableMetadata getSonMetadata(Connection connection){
        return TableUtils.getTableMetadata(getSonName(), connection);
    }

    public TableMetadata getParentMetadata(Connection connection){
        return TableUtils.getTableMetadata(getParentName(), connection);
    }

    public Nest fill(){
        id = IdUtils.createShort8Id();
        createdAt = ServiceUtils.now();
        return this;
    }
}

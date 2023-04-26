package com.black.core.sql.entity;

import com.black.core.query.ClassWrapper;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EntityConfigurer {

    private final Class<?> entityType;

    private final ClassWrapper<?> cw;

    //对实体类表名, 表字段进行检验
    private boolean verification = true;

    //主键名称, 如果允许检验, 则该值会自动寻找
    private String primaryKeyName;

    private String tableName;

    public EntityConfigurer(Class<?> entityType) {
        this.entityType = entityType;
        cw = ClassWrapper.get(entityType);
    }
}

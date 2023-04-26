package com.black.core.sql.entity;


import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.annotation.PrimaryKey;
import com.black.core.sql.annotation.SkipVerification;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.util.StringUtils;
import com.black.utils.ReflexHandler;
import lombok.NonNull;

public class EntityNativeManager {


    public static EntityConfigurer parseMapper(@NonNull Class<? extends EntityMapper> mapperType, @NonNull GlobalSQLConfiguration configuration) {
        Class<?>[] genericVal = ReflexHandler.genericVal(mapperType, EntityMapper.class);
        if (genericVal.length != 1){
            throw new IllegalStateException("unknown mapper: [" + mapperType.getSimpleName() + "]");
        }
        return parseEntity(genericVal[0], configuration);
    }

    public static EntityConfigurer parseEntity(@NonNull Class<?> entityType, @NonNull GlobalSQLConfiguration configuration){
        EntityConfigurer configurer = new EntityConfigurer(entityType);
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        TableName tableName = entityType.getAnnotation(TableName.class);
        //获取表名
        String name = tableName != null ? tableName.value() : convertHandler.convertColumn(StringUtils.titleLower(entityType.getSimpleName()));
        configurer.setTableName(name);

        ClassWrapper<?> cw = configurer.getCw();
        FieldWrapper fieldWrapper = cw.getSingleFieldByAnnotation(PrimaryKey.class);
        if (fieldWrapper != null){
            configurer.setPrimaryKeyName(fieldWrapper.getName());
        }

        if (cw.hasAnnotation(SkipVerification.class)){
            configurer.setVerification(false);
        }
        return configurer;
    }
}

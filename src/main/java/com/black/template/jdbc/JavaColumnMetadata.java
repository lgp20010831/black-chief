package com.black.template.jdbc;

import com.black.project.DEMO;
import com.black.core.util.Av0;
import com.black.core.util.StringUtils;
import com.black.table.DefaultColumnMetadata;
import lombok.Getter;

@Getter
public class JavaColumnMetadata extends DefaultColumnMetadata {

    Class<?> javaClass;

    String javaTypeName;

    String javaTypePath;

    String javaFieldName;

    String nameOfGet;

    boolean applyLike;

    String fieldAnnotation = "@TableField(value = \"" + name + "\")";

    public JavaColumnMetadata(DefaultColumnMetadata defaultColumnMetadata){
        this(defaultColumnMetadata.getName(), defaultColumnMetadata.getSize(), defaultColumnMetadata.getTypeName(), defaultColumnMetadata.getType());
        String remarks = defaultColumnMetadata.getRemarks();
        if (StringUtils.hasText(remarks)){
            remarks = remarks.replace("\r\n", "");
            remarks = remarks.replace("\t", "");
            remarks = remarks.replace("\n", "");
            remarks = remarks.replace("\r", "");
        }
        setRemarks(remarks);
    }

    public JavaColumnMetadata(String name, int size, String typeName, int type) {
        super(name, size, typeName, type);
        javaClass = JdbcType.getByJdbcType(type).javaClass;
        if (javaClass == null){
            javaClass = String.class;
        }
        applyLike = !(Number.class.isAssignableFrom(javaClass) || Boolean.class.equals(javaClass));
        javaTypeName = javaClass.getSimpleName();
        javaTypePath = javaClass.getName();
        javaFieldName = Av0.ruacnl(name);
        nameOfGet = "get" + StringUtils.titleCase(javaFieldName);
        if (javaFieldName.equals(DEMO.UPDATE_FIELD) || DEMO.UPDATE_FIELD_SET.contains(javaFieldName)){
            fieldAnnotation = "@TableField(value = \"" + name + "\", fill = FieldFill.INSERT_UPDATE)";
        }else if (javaFieldName.equals(DEMO.INSERTED_FIELD) || DEMO.INSERTED_FIELD_SET.contains(javaFieldName)){
            fieldAnnotation = "@TableField(value = \"" + name + "\", fill = FieldFill.INSERT)";
        }
    }

    public static void main(String[] args) {
        JdbcType jdbcType = JdbcType.getByJdbcType(5);
        System.out.println(jdbcType.javaClass);
    }
}

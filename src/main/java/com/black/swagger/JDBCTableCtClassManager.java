package com.black.swagger;

import com.black.function.Consumer;
import com.black.javassist.Utils;
import com.black.core.util.StringUtils;
import com.black.table.ColumnMetadata;
import com.black.table.DefaultColumnMetadata;
import com.black.table.TableMetadata;
import com.black.template.jdbc.JavaColumnMetadata;
import com.black.utils.ServiceUtils;
import io.swagger.annotations.ApiModelProperty;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JDBCTableCtClassManager {

    private static Map<String, Class<?>> tableWithCtClass = new ConcurrentHashMap<>();

    //将 javassist 动态生成的类生成到当前包路径下
    public static String FICTITIOUS_PATH = "com.example.fictitious";

    public static Class<?> getCtClass(@NonNull TableMetadata tableMetadata){
        String tableName = tableMetadata.getTableName();
        if (tableWithCtClass.containsKey(tableName)){
            return tableWithCtClass.get(tableName);
        }
        String className = StringUtils.titleCase(StringUtils.ruacnl(tableName));
        String fullName = FICTITIOUS_PATH + "." + className;
        CtClass ctClass = Utils.createClass(fullName);
        ConstPool constPool = ctClass.getClassFile().getConstPool();
        Collection<ColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadatas();
        ArrayList<CtField> fields = new ArrayList<>();
        for (ColumnMetadata columnMetadata : columnMetadatas) {
            JavaColumnMetadata javaColumnMetadata = new JavaColumnMetadata((DefaultColumnMetadata) columnMetadata);
            Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annMap = ServiceUtils.ofMap(ApiModelProperty.class, (Consumer<Annotation>) annotation -> {
                String remarks = columnMetadata.getRemarks();
                annotation.addMemberValue("value", new StringMemberValue(remarks == null ? "" : remarks, constPool));

            });
            CtField field = Utils.createField(javaColumnMetadata.getName(), javaColumnMetadata.getJavaClass(), annMap, ctClass);
            fields.add(field);
        }
        Class<?> javaClass = Utils.createJavaClass(fields, ctClass);
        tableWithCtClass.put(tableName, javaClass);
        return javaClass;
    }

}

package com.black.javassist;

import com.black.api.ApiV2Utils;
import com.black.function.Consumer;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.table.ColumnMetadata;
import com.black.table.DefaultColumnMetadata;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.template.jdbc.JavaColumnMetadata;
import javassist.CtField;
import javassist.bytecode.annotation.Annotation;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("all")
public class JdbcEntityCreator {

    private Connection connection;

    private ColumnAnnotationGenerator columnAnnotationGenerator;

    public JdbcEntityCreator(){}

    public JdbcEntityCreator(@NonNull DataSource dataSource){
        try {
            setConnection(dataSource.getConnection());
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public JdbcEntityCreator(Connection connection){
        setConnection(connection);
    }

    public void setConnection(Connection connection) {
        if (this.connection == connection){
            return;
        }
        if (!(connection instanceof DatabaseUniquenessConnectionWrapper)){
            this.connection = ApiV2Utils.wrapperConnection(connection);
        }else {
            this.connection = connection;
        }
    }

    public ColumnAnnotationGenerator getColumnAnnotationGenerator() {
        return columnAnnotationGenerator;
    }

    public void setColumnAnnotationGenerator(ColumnAnnotationGenerator columnAnnotationGenerator) {
        this.columnAnnotationGenerator = columnAnnotationGenerator;
    }

    public Class<?> create(String tableName){
        String classPath = Utils.FICTITIOUS_PATH;
        String className = StringUtils.titleCase(StringUtils.ruacnl(tableName));
        String classFullName = classPath + "." + className;
        try {
            return Class.forName(classFullName);
        } catch (ClassNotFoundException e) {

        }
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(className, classPath);
        return partiallyCtClass.getJavaClass();
    }

    public Class<?> createJavaClass(String tableName){
        return createJavaClass(tableName, Utils.FICTITIOUS_PATH);
    }

    public Class<?> createJavaClass(String tableName, String classPath){
        String className = StringUtils.titleCase(StringUtils.ruacnl(tableName));
        String classFullName = classPath + "." + className;
        try {
            return Class.forName(classFullName);
        } catch (ClassNotFoundException e) {

        }
        Assert.notNull(connection, "not find jdbc connection");
        try {
            if (connection.isClosed()) {
                throw new SQLSException("connection is closed");
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        DatabaseUniquenessConnectionWrapper uniquenessConnectionWrapper = (DatabaseUniquenessConnectionWrapper) connection;
        String key = uniquenessConnectionWrapper.getDatabaseName() + "." + tableName;
        return JavassistCtClassManager.getCtClass(key, () -> {
            return createJavaClass0(tableName, classPath, className);
        });

    }

    private Class<?> createJavaClass0(String tableName, String classPath, String className){
        TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(className, classPath);
        Collection<ColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadatas();
        for (ColumnMetadata columnMetadata : columnMetadatas) {
            JavaColumnMetadata javaColumnMetadata = new JavaColumnMetadata((DefaultColumnMetadata) columnMetadata);
            Map<Class<? extends java.lang.annotation.Annotation>,
                    Consumer<Annotation>> annotationMap = null;
            if (columnAnnotationGenerator != null) {
                CtAnnotations annotations = columnAnnotationGenerator.createAnnotations(javaColumnMetadata);
                annotationMap = annotations.getAnnotationCallback();
            }
            CtField field = Utils.createField(javaColumnMetadata.getJavaFieldName(),
                    javaColumnMetadata.getJavaClass(), annotationMap,
                    partiallyCtClass.getCtClass());
            partiallyCtClass.addField(field);
        }
        return partiallyCtClass.getJavaClass();
    }

    public void close(){
        SQLUtils.closeConnection(connection);
    }
}

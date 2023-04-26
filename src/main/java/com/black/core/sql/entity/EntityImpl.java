package com.black.core.sql.entity;

import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StreamUtils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.ServiceUtils;
import lombok.NonNull;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityImpl implements EntityMapper<Object>{

    private final GlobalSQLConfiguration configuration;

    private final GlobalParentMapping parentMapping;

    private final Class<? extends EntityMapper<?>> targetMapper;

    private final EntityConfigurer entityConfigurer;

    private final String tableName;

    private final Class<?> entityType;

    private String primaryKeyName;

    private String primaryColumnName;

    public EntityImpl(@NonNull GlobalSQLConfiguration configuration,
                      @NonNull GlobalParentMapping parentMapping,
                      Class<? extends EntityMapper<?>> targetMapper) {
        this.configuration = configuration;
        this.parentMapping = parentMapping;
        this.targetMapper = targetMapper;
        entityConfigurer = EntityNativeManager.parseMapper(this.targetMapper, configuration);
        tableName = entityConfigurer.getTableName();
        entityType = entityConfigurer.getEntityType();
        primaryColumnName = entityConfigurer.getPrimaryKeyName();
        if (primaryColumnName == null){
            String alias = configuration.getDataSourceAlias();
            Connection connection = ConnectionManagement.getConnection(alias);
            try {
                TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
                Assert.notNull(metadata, "unknown table: " + tableName + ", of mapper: " + targetMapper);
                PrimaryKey primaryKey = metadata.firstPrimaryKey();
                primaryColumnName = primaryKey == null ? null : primaryKey.getName();
            }finally {
                if (!TransactionSQLManagement.isActivity(alias)) {
                    ConnectionManagement.closeCurrentConnection(alias);
                }
            }
        }
        primaryKeyName = primaryColumnName == null ? null : configuration.getConvertHandler().convertAlias(primaryColumnName);

    }

    private void checkPrimary(){
        if (primaryColumnName == null && primaryKeyName == null){
            throw new UnsupportedOperationException("can not find primary key of table: " + tableName);
        }
    }

    public List<Object> convertList(List<Map<String, Object>> dataList, Class<?> type){
        return StreamUtils.mapList(dataList, data -> convert(data, type));
    }

    private Object convert(Map<String, Object> data, Class<?> type){
       return BeanUtil.mapping(ReflexUtils.instance(type), data);
    }

    private Map<String, Object> toMap(Object data){
        return JsonUtils.letJson(data);
    }

    private Object getIdByEntity(Object entity){
        return SetGetUtils.invokeGetMethod(primaryKeyName, entity);
    }

    @Override
    public Object findById(Object id) {
        checkPrimary();
        return convert(parentMapping.findById(tableName, primaryColumnName, id), entityType);
    }

    @Override
    public Object find(Object condition) {
        return convert(parentMapping.globalSelectSingle(tableName, toMap(condition)), entityType);
    }

    @Override
    public List<Object> findAll() {
        return convertList(parentMapping.globalSelect(tableName, null), entityType);
    }

    @Override
    public List<Object> findAll(Object condition) {
        return convertList(parentMapping.globalSelect(tableName, toMap(condition)), entityType);
    }

    @Override
    public List<Object> findAllById(Iterable<Object> ids) {
        checkPrimary();
        return convertList(parentMapping.findAllById(tableName, primaryColumnName, ids), entityType);
    }

    @Override
    public boolean existsById(Object id) {
        checkPrimary();
        return parentMapping.existsById(tableName, primaryColumnName, id);
    }

    @Override
    public int count() {
        return parentMapping.count(tableName, null);
    }

    @Override
    public Object save(Object o) {
        checkPrimary();
        Object id = getIdByEntity(o);
        if (id == null){
            id = parentMapping.globalInsertSingle(tableName, toMap(o));
        }else {
            parentMapping.globalUpdate(tableName, toMap(o), ServiceUtils.ofMap(primaryKeyName, id));
        }
        return findById(id);
    }

    @Override
    public List<Object> saveBatch(Collection<Object> collection) {
        return StreamUtils.mapList(collection, this::save);
    }

    @Override
    public void deleteById(Object var1) {
        checkPrimary();
        parentMapping.deleteById(tableName, primaryColumnName, var1);
    }

    @Override
    public void delete(Object condition) {
        parentMapping.globalDelete(tableName, toMap(condition));
    }

    @Override
    public void deleteAll(Collection<Object> collection) {
        for (Object o : collection) {
            delete(o);
        }
    }

    @Override
    public void deleteAll() {
        parentMapping.globalDelete(tableName, null);
    }

    @Override
    public void configureSyntax(SyntaxConfigurer syntaxConfigurer, int validReferences) {
        parentMapping.configureSyntax(syntaxConfigurer, validReferences);
    }
}

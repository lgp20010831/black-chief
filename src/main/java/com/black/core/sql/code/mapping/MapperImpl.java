package com.black.core.sql.code.mapping;

import com.black.core.query.ClassWrapper;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.ServiceUtils;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MapperImpl implements Mapper{

    private final GlobalSQLConfiguration configuration;

    private final GlobalParentMapping parentMapping;

    private final Class<? extends Mapper> targetMapper;

    private String tableName;

    private final String primaryKeyName;

    private final String primaryColumnName;

    public MapperImpl(GlobalSQLConfiguration configuration, GlobalParentMapping parentMapping, Class<? extends Mapper> targetMapper) {
        this.configuration = configuration;
        this.parentMapping = parentMapping;
        this.targetMapper = targetMapper;
        tableName = parseTableName(targetMapper);
        String alias = configuration.getDataSourceAlias();
        Connection connection = ConnectionManagement.getConnection(alias);
        try {
            TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
            Assert.notNull(metadata, "unknown table: " + tableName + ", of mapper: " + targetMapper);
            PrimaryKey primaryKey = metadata.firstPrimaryKey();
            primaryColumnName = primaryKey == null ? null : primaryKey.getName();
            primaryKeyName = primaryColumnName == null ? null : configuration.getConvertHandler().convertAlias(primaryColumnName);
        }finally {
            if (!TransactionSQLManagement.isActivity(alias)) {
                ConnectionManagement.closeCurrentConnection(alias);
            }
        }
    }

    private void checkPrimary(){
        if (primaryColumnName == null && primaryKeyName == null){
            throw new UnsupportedOperationException("can not find primary key of table: " + tableName);
        }
    }


    private String parseTableName(Class<? extends Mapper> targetMapper){
        ClassWrapper<? extends Mapper> wrapper = ClassWrapper.get(targetMapper);
        if (wrapper.hasAnnotation(TableName.class)) {
            return wrapper.getAnnotation(TableName.class).value();
        }

        String simpleName = wrapper.getSimpleName();
        simpleName = StringUtils.removeIfEndWith(simpleName, "SqlMapper");
        simpleName = StringUtils.removeIfEndWith(simpleName, "Mapper");
        simpleName = StringUtils.removeIfEndWith(simpleName, "Dao");
        simpleName = StringUtils.removeIfEndWith(simpleName, "Repository");
        return StringUtils.unruacnl(StringUtils.titleLower(simpleName));
    }


    private Object getIdByMap(Map<String, Object> map){
        return map.get(primaryKeyName);
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Map<String, Object> findById(Object id) {
        checkPrimary();
        return parentMapping.globalSelectSingle(getTableName(), ServiceUtils.ofMap(primaryKeyName, id));
    }

    @Override
    public Map<String, Object> find(Map<String, Object> condition) {
        return parentMapping.globalSelectSingle(getTableName(), condition);
    }

    @Override
    public List<Map<String, Object>> findAll() {
        return parentMapping.globalSelect(getTableName(), null);
    }

    @Override
    public List<Map<String, Object>> findAll(Map<String, Object> condition) {
        return parentMapping.globalSelect(getTableName(), condition);
    }

    @Override
    public List<Map<String, Object>> findAllById(Iterable<Object> ids) {
        checkPrimary();
        return parentMapping.findAllById(getTableName(), primaryColumnName, ids);
    }

    @Override
    public boolean existsById(Object id) {
        checkPrimary();
        return parentMapping.existsById(getTableName(), primaryColumnName, id);
    }

    @Override
    public int count() {
        return parentMapping.count(getTableName(), null);
    }

    @Override
    public int count(Map<String, Object> map) {
        return parentMapping.count(getTableName(), map);
    }

    @Override
    public Map<String, Object> save(Map<String, Object> e) {
        checkPrimary();
        Object id = getIdByMap(e);
        if (id == null){
            id = parentMapping.globalInsertSingle(tableName, e);
        }else {
            parentMapping.globalUpdate(tableName, e, ServiceUtils.ofMap(primaryKeyName, id));
        }
        return findById(id);
    }

    @Override
    public List<Map<String, Object>> saveBatch(Collection<Map<String, Object>> collection) {
        return StreamUtils.mapList(collection, this::save);
    }

    @Override
    public void deleteById(Object var1) {
        checkPrimary();
        parentMapping.deleteById(getTableName(), primaryColumnName, var1);
    }

    @Override
    public void delete(Map<String, Object> condition) {
        parentMapping.globalDelete(tableName, condition);
    }

    @Override
    public void deleteAll(Collection<Map<String, Object>> collection) {
        for (Map<String, Object> o : collection) {
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

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Connection getConnection() {
        return configuration.getConnection();
    }

    @Override
    public GlobalParentMapping getParent() {
        return parentMapping;
    }

    @Override
    public TableMetadata getMetadata() {
        return parentMapping.getMetadata(getTableName());
    }

    @Override
    public String getAlias() {
        return configuration.getDataSourceAlias();
    }

    @Override
    public String showTableInfo() {
        return parentMapping.showTableInfo(getTableName());
    }

    @Override
    public Map<String, Object> globalSelectSingle(Map<String, Object> map) {
        return parentMapping.globalSelectSingle(getTableName(), map);
    }

    @Override
    public Map<String, Object> globalSelectSingle(Map<String, Object> map, String blendString) {
        return parentMapping.globalSelectSingle(getTableName(), map, blendString);
    }

    @Override
    public List<Map<String, Object>> globalSelect(Map<String, Object> map) {
        return parentMapping.globalSelect(getTableName(), map);
    }

    @Override
    public List<Map<String, Object>> globalSelect(Map<String, Object> map, String blendString) {
        return parentMapping.globalSelect(getTableName(), map, blendString);
    }

    @Override
    public Map<String, Object> globalDictSelectSingle(Map<String, Object> map, String... dictionary) {
        return parentMapping.globalDictSelectSingle(getTableName(), map, dictionary);
    }

    @Override
    public Map<String, Object> globalDictSelectSingle(Map<String, Object> map, String blendString, String... dictionary) {
        return parentMapping.globalDictSelectSingle(getTableName(), map, blendString, dictionary);
    }

    @Override
    public List<Map<String, Object>> globalDictSelect(Map<String, Object> map, String... dictionary) {
        return parentMapping.globalDictStringSelect(getTableName(), map, dictionary);
    }

    @Override
    public List<Map<String, Object>> globalDictSelect(Map<String, Object> map, String blendString, String... dictionary) {
        return parentMapping.globalDictSelect(getTableName(), map, blendString, dictionary);
    }

    @Override
    public Map<String, Object> globalSyntaxSelectSingle(Map<String, Object> map, SyntaxConfigurer configurer) {
        return parentMapping.globalSyntaxSelectSingle(getTableName(), map, configurer);
    }

    @Override
    public List<Map<String, Object>> globalSyntaxSelect(Map<String, Object> map, SyntaxConfigurer configurer) {
        return parentMapping.globalSyntaxSelect(getTableName(), map, configurer);
    }
}

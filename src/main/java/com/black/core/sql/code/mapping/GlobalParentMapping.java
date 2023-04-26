package com.black.core.sql.code.mapping;

import com.black.core.annotation.Parent;
import com.black.core.json.Alias;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.*;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.config.SyntaxRangeConfigurer;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.PrepareStatementFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.fast.annotation.FastInsert;
import com.black.core.sql.run.RunSqlParser;
import com.black.core.sql.run.SqlRunner;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.sql.NativeSql;
import com.black.sql.QueryResultSetParser;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.table.jdbc.CatalogConnection;
import com.black.utils.ServiceUtils;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 具体实现 -->
 * {@link com.black.core.sql.code.MapperSQLProxy}
 */
@Parent  @SuppressWarnings("all")
public interface GlobalParentMapping {


    //=====================================================================================
    //                                  查询
    //=====================================================================================

    default List<Map<String, Object>> fullApplySelect(String name, Map<String, Object> condition, String applySql){
        return fullSelect(name, condition, null, applySql, null, null);
    }

    default List<Map<String, Object>> fullApplySelect(String name, Map<String, Object> condition, String applySql, Collection<String> sqlSeqs){
        return fullSelect(name, condition, null, applySql, sqlSeqs, null);
    }

    default List<Map<String, Object>> fullDictBlendSelect(String name, Map<String, Object> condition, String blendString, Collection<String> dicts){
        return fullSelect(name, condition, blendString, null, null, dicts);
    }

    default List<Map<String, Object>> fullDictSelect(String name, Map<String, Object> condition, String applySql, Collection<String> dicts){
        return fullSelect(name, condition, null, applySql, null, dicts);
    }

    default List<Map<String, Object>> fullDictSelect(String name, Map<String, Object> condition, Collection<String> dicts){
        return fullSelect(name, condition, null, null, null, dicts);
    }

    default List<Map<String, Object>> fullDictSelect(String name, Map<String, Object> condition, Collection<String> dicts, Collection<String> sqlSeqs){
        return fullSelect(name, condition, null, null, sqlSeqs, dicts);
    }

    default List<Map<String, Object>> fullSelect(String name, Map<String, Object> condition, String blendString,
                                                 String applySql, Collection<String> sqlSeqs, Collection<String> dicts){
        String alias = getAlias();
        SyntaxRangeConfigurer rangeConfigurer = SyntaxManager.getRangeConfigurer(alias);
        SyntaxConfigurer configurer;
        if(rangeConfigurer == null){
            configurer = new SyntaxConfigurer();
        }else {
            configurer = rangeConfigurer.getConfigurer();
        }
        if (StringUtils.hasText(applySql))
            configurer.applySql(applySql);
        if (StringUtils.hasText(blendString))
            configurer.setBlendSyntax(blendString);
        if (sqlSeqs != null)
            configurer.addSequences(sqlSeqs.toArray(new String[0]));
        if (dicts != null)
            configurer.addDicts(dicts.toArray(new String[0]));
        configureSyntax(configurer, 1);
        return globalDictSelect(name, condition, null);
    }

    default List<Map<String, Object>> findAll(String name){
        return globalSelect(name, null);
    }

    default Map<String, Object> findById(String name, Object value){
        return findById(name, findIdOfTable(name), value);
    }

    default Map<String, Object> findById(String name, String primaryName, Object value){
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        String alias = convertHandler.convertAlias(primaryName);
        return globalSelectSingle(name, ServiceUtils.ofMap(alias, value));
    }

    default List<Map<String, Object>> findAllById(String name, Iterable<Object> value){
        return findAllById(name, findIdOfTable(name), value);
    }

    default String findIdOfTable(String name){
        return ConnectionManagement.employConnection(getAlias(), connection -> {
            TableMetadata metadata = TableUtils.getTableMetadata(name, connection);
            Assert.notNull(metadata, "unknown table of:[" + name + "]");
            PrimaryKey primaryKey = metadata.firstPrimaryKey();
            return primaryKey == null ? null : primaryKey.getName();
        });
    }

    default List<Map<String, Object>> findAllById(String name, String primaryName, Iterable<Object> value){
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        String alias = convertHandler.convertAlias(primaryName);
        return globalSelect(name, ServiceUtils.ofMap(alias, value), "in[" + alias + "]");
    }

    default boolean existsById(String name, Object id){
        return findById(name, id) != null;
    }

    default boolean existsById(String name, String primaryName, Object id){
        return findById(name, primaryName, id) != null;
    }

    @SelectCount
    @Configurer @Query
    int count(@TableName String name, @BlendMap @Param Map<String, Object> map);

    default Map<String, Object> globalSelectSingle(String name, Map<String, Object> map){
        return globalSelectSingle(name, map, null);
    }

    default Map<String, Object> globalSelectSingle(String name, Map<String, Object> map, String blendString){
        List<Map<String, Object>> mapList = globalSelect(name, map, blendString);
        return SQLUtils.getSingle(mapList);
    }

    default List<Map<String, Object>> globalSelect(String name, Map<String, Object> map){
        return globalSelect(name, map, null);
    }

    @Query
    @Configurer
    List<Map<String, Object>> globalSelect(@TableName String name, @BlendMap @Param Map<String, Object> map, @BlendString String blendString);


    default Map<String, Object> globalDictSelectSingle(String name,
                                                       Map<String, Object> map,
                                                       String... dictionary){
        return globalDictSelectSingle(name, map, null, dictionary);
    }

    default Map<String, Object> globalDictSelectSingle(String name,
                                                       Map<String, Object> map,
                                                       String blendString,
                                                       String... dictionary){
        List<Map<String, Object>> maps = globalDictSelect(name, map, blendString, dictionary);
        return SQLUtils.getSingle(maps);

    }

    default List<Map<String, Object>> globalDictStringSelect(String name,
                                                       Map<String, Object> map,
                                                       String... dictionary){
        return globalDictSelect(name, map, null, dictionary);
    }

    default List<Map<String, Object>> globalDictBlendStringSelect(String name,
                                                             Map<String, Object> map,
                                                             String blendString){
        return globalDictSelect(name, map, blendString);
    }

    @Query
    @Configurer
    @Dictionary
    List<Map<String, Object>> globalDictSelect(@TableName String name,
                                               @BlendMap @Param Map<String, Object> map,
                                               @BlendString String blendString,
                                               @DictionaryString String... dictionary);


    default Map<String, Object> globalSyntaxSelectSingle(String name,
                                                         Map<String, Object> map,
                                                         SyntaxConfigurer configurer){
        List<Map<String, Object>> list = globalSyntaxSelect(name, map, configurer);
        return SQLUtils.getSingle(list);
    }

    @Query
    @Configurer
    @Dictionary
    List<Map<String, Object>> globalSyntaxSelect(@TableName String name,
                                                 @BlendMap @Param Map<String, Object> map,
                                                 SyntaxConfigurer configurer);
    //=====================================================================================
    //                                  添加
    //=====================================================================================
    @Insert
    @Configurer
    String globalInsertSingle(@TableName String name, Map<String, Object> map);

    @Insert
    @Configurer
    List<String> globalInsertBatch(@TableName String name, List<Map<String, Object>> list);

    @Insert
    @Configurer
    @UnsupportPlatform
    List<String> UnsupportPlatformInsertBatch(@TableName String name, List<Map<String, Object>> list);


    default Boolean fastJoin(String name, Map<String, Object> body){
        return fastJoin(name, Collections.singletonList(body));
    }

    /** 快速添加 */
    @FastInsert(parseResult = true)
    Boolean fastJoin(@TableName String name, @Param List<Map<String, Object>> list);

    /** 添加时判断数据是否存在 */
    @Configurer
    @AutomaticallyUpdateOrInsert
    List<String> saveOrUpdate(@TableName String name, List<Map<String, Object>> list);

    default List<Map<String, Object>> saveBatch(String name, List<Map<String, Object>> mapList){
        return StreamUtils.mapList(mapList, map -> {
            return save(name, map);
        });
    }

    default Map<String, Object> save(String name, Map<String, Object> map){
        String idName = findIdOfTable(name);
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        String alias = convertHandler.convertAlias(idName);
        Object id = map.get(alias);
        if (id == null){
            id = globalInsertSingle(name, map);
        }else {
            globalUpdate(name, map, ServiceUtils.ofMap(alias, id));
        }
        return findById(name, idName, id);
    }

    default List<Map<String, Object>> saveIfExistBatch(String name, List<Map<String, Object>> mapList){
        return StreamUtils.mapList(mapList, map -> {
            return saveIfExist(name, map);
        });
    }

    default Map<String, Object> saveIfExist(String name, Map<String, Object> map){
        String idName = findIdOfTable(name);
        AliasColumnConvertHandler convertHandler = getConvertHandler();
        String alias = convertHandler.convertAlias(idName);
        Object id = map.get(alias);
        if (id == null){
            id = globalInsertSingle(name, map);
        }else {
            Map<String, Object> single = globalSelectSingle(name, ServiceUtils.ofMap(alias, id));
            if (single == null){
                id = globalInsertSingle(name, map);
            }else {
                globalUpdate(name, map, ServiceUtils.ofMap(alias, id));
            }
        }
        return findById(name, idName, id);
    }

    //=====================================================================================
    //                                  更新
    //=====================================================================================

    default String updateById(String name,
                              Map<String, Object> setMap,
                              Object idValue){
        String idName = findIdOfTable(name);
        return globalUpdate(name, setMap, ServiceUtils.ofMap(idName, idValue));
    }

    default String globalUpdate(String name,
                                Map<String, Object> setMap,
                                Map<String, Object> condition){
        return globalUpdate(name, setMap, condition, null);
    }

    @Renew
    @Configurer
    @ProvidePlatform({"update", "set"})
    String globalUpdate(@TableName String name,
                        @SetMap @Param Map<String, Object> setMap,
                        @BlendMap @Param Map<String, Object> condition,
                        @BlendString String blend);

    /** 更新时判断数据是否存在, 不存在则插入一条 */
    @Configurer
    @AutomaticallyUpdateOrInsert
    String updateOrSave(@TableName String name, @SetMap @Param Map<String, Object> setMap, @BlendMap @Param Map<String, Object> condition);
    //=====================================================================================
    //                                  删除
    //=====================================================================================
    default boolean globalDelete(String name,Map<String, Object> map){
        return globalDelete(name, map, null);
    }

    @Delete
    @Configurer
    boolean globalDelete(@TableName String name, @BlendMap @Param Map<String, Object> map, @BlendString String blend);

    default boolean deleteById(String name, Object value){
        return deleteById(name, findIdOfTable(name), value);
    }

    default boolean deleteById(String name, String primaryName, Object value){
        return globalDelete(name, ServiceUtils.ofMap(primaryName, value));
    }

    default boolean deleteAllById(String name, Iterable<Object> iterable){
        return deleteAllById(name, findIdOfTable(name), iterable);
    }

    default boolean deleteAllById(String name, String primaryName, Iterable<Object> iterable){
        return globalDelete(name, ServiceUtils.ofMap(primaryName, iterable), "in[" + primaryName + "]");
    }

    default void deleteAll(String name){
        globalDelete(name, null);
    }

    //=====================================================================================
    //                                  刷新
    //=====================================================================================
    /** 刷新表结构, 刷新的是所有表结构 */
    @RefreshTable
    void refresh();

    //=====================================================================================
    //                                  获取连接
    //=====================================================================================
    @ObtainConnection
    CatalogConnection getConnection();

    //不要擅自关闭连接
    default Connection getFetchConnection(){
        return getConfiguration().getConnection();
    }

    //=====================================================================================
    //                                  占用符
    //=====================================================================================
    @DoNothing
    Object doNothing();

    //=====================================================================================
    //                                  base
    //=====================================================================================
    @Alias("getAlias")
    String getAlias();

    @Alias("showTableInfo")
    String showTableInfo(String name);

    default AliasColumnConvertHandler getConvertHandler(){
        return getConfiguration().getConvertHandler();
    }

    default void configureSyntax(SyntaxConfigurer syntaxConfigurer, int validReferences){
        if (validReferences < 1){
            throw new IllegalArgumentException("引用数必须大于或等于 1 ");
        }
        SyntaxRangeConfigurer rangeConfigurer = new SyntaxRangeConfigurer(syntaxConfigurer, getAlias());
        rangeConfigurer.setValidReferences(validReferences);
        SyntaxManager.registerSyntax(rangeConfigurer);
    }

    GlobalSQLConfiguration getConfiguration();

    default TableMetadata getMetadata(String name){
        return ConnectionManagement.employConnection(getAlias(), connection -> {
            return TableUtils.getTableMetadata(name, connection);
        });
    }

    //=====================================================================================
    //                                  jdbc template
    //=====================================================================================

    default void commit(){
        String alias = getAlias();
        Connection fetchConnection = getFetchConnection();
        try {
            if (!fetchConnection.getAutoCommit()){
                fetchConnection.commit();
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            if (!TransactionSQLManagement.isActivity(alias)) {
                ConnectionManagement.closeCurrentConnection(alias);
            }
        }
    }

    default void rollback(){
        String alias = getAlias();
        Connection fetchConnection = getFetchConnection();
        try {
            fetchConnection.rollback();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            if (!TransactionSQLManagement.isActivity(alias)) {
                ConnectionManagement.closeCurrentConnection(alias);
            }
        }
    }

    default Map<String, Object> executeSingleSelectSql(String sql, Map<String, Object> condition){
        return SQLUtils.getSingle(executeSelectSql(sql, condition));
    }

    @SelectScript
    List<Map<String, Object>> executeSelectSql(@Param String sql, Map<String, Object> condition);

    default QueryResultSetParser nativeSql(String sql, Object... array){
        return NativeSql.createQuery(sql, getAlias(), array);
    }

    default int executeUpdateSql(@NonNull String sql, Map<String, Object> argMap){
        String alias = getAlias();
        GlobalSQLConfiguration configuration = getConfiguration();
        Connection connection = ConnectionManagement.getConnection(alias);
        try {
            Log log = configuration.getLog();
            sql = GlobalMapping.parseAndObtain(sql, true);
            sql = RunSqlParser.parseSql(sql, argMap);
            if (log != null && log.isDebugEnabled()) {
                log.debug("==> execute sql: [" + sql + "]");
            }
            Object result = null;
            StatementWrapper statementWrapper = PrepareStatementFactory.getStatement(sql, configuration, connection, false);
            SqlRunner runner = new SqlRunner();
            ExecuteBody executeBody = runner.doExecute(statementWrapper, false);
            return executeBody.getUpdateCount();
        } catch (SQLException throwables) {
            throw new SQLSException(throwables);
        } finally {
            if (!TransactionSQLManagement.isActivity(alias)){
                ConnectionManagement.closeCurrentConnection(alias);
            }
        }

    }
}

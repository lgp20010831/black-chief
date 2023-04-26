package com.black.nest;

import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.javassist.DatabaseUniquenessConnectionWrapper;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.run.code.ChiefSqlScriptRunner;
import com.black.core.run.code.Configuration;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.utils.ServiceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NestManager {

    public static String QUERY_ALL_DICT_SQL = "select * from t_dict";

    public static String QUERY_DICT_BY_SOURCE_NAME = "select * from t_dict where source_table_name = #{?}";

    public static String QUERY_ALL_NEST_SQL = "select * from t_nest";

    public static String QUERY_NEST_BY_PARENT_NAME = "select * from t_nest where parent_name = #{?}";

    private static final IoLog log = LogFactory.getArrayLog();

    private static final Map<String, List<Nest>> nestCache = new ConcurrentHashMap<>();

    private static final Map<String, List<Dict>> dictCache = new ConcurrentHashMap<>();

    private static boolean useCache = false;

    private static volatile boolean init = false;


    public static void putDictCache(String name, Dict dict){
        List<Dict> dicts = dictCache.computeIfAbsent(name, n -> new ArrayList<>());
        dicts.add(dict);
    }

    public static void putDictCache(String name, List<Dict> dictList){
        List<Dict> dicts = dictCache.computeIfAbsent(name, n -> new ArrayList<>());
        dicts.addAll(dictList);
    }
    public static void putNestCache(String name, Nest nest){
        List<Nest> nests = nestCache.computeIfAbsent(name, n -> new ArrayList<>());
        nests.add(nest);
    }

    public static void putNestCache(String name, List<Nest> nestList){
        List<Nest> nests = nestCache.computeIfAbsent(name, n -> new ArrayList<>());
        nests.addAll(nestList);
    }

    public static void setUseCache(boolean useCache) {
        NestManager.useCache = useCache;
    }

    public static boolean isUseCache() {
        return useCache;
    }

    protected static DatabaseUniquenessConnectionWrapper getWrapperConnection(Connection connection){
        if (connection instanceof DatabaseUniquenessConnectionWrapper){
            return (DatabaseUniquenessConnectionWrapper) connection;
        }else {
            return new DatabaseUniquenessConnectionWrapper(connection);
        }
    }


    public synchronized static void init(Class<? extends DataSourceBuilder> type){
        DataSourceBuilder builder = DataSourceBuilderTypeManager.getBuilder(type);
        DataSource dataSource = builder.getDataSource();
        try {
            Connection connection = dataSource.getConnection();
            init(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public synchronized static void init(Connection connection){
        if (init){
            return;
        }
        ChiefSqlScriptRunner scriptRunner = new ChiefSqlScriptRunner();
        Configuration configuration = scriptRunner.getConfiguration();
        configuration.setConnection(connection);
        scriptRunner.runScript("nest/nest_v1.sql");
        init = true;
    }

    public static List<Nest> queryAllNest(Connection connection){
        if (isUseCache()){
            DatabaseUniquenessConnectionWrapper connectionWrapper = getWrapperConnection(connection);
            String databaseName = connectionWrapper.getDatabaseName();
            return new ArrayList<>(nestCache.getOrDefault(databaseName, new ArrayList<>()));
        }
        if (!init){
            init(connection);
        }
        log.info("[NestManager] -- run query sql: {}", QUERY_ALL_NEST_SQL);
        List<Map<String, Object>> maps;
        try {
            maps = SQLUtils.runJavaSelect(QUERY_ALL_NEST_SQL, connection, new HumpColumnConvertHandler());
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        return BeanUtil.toBeanBatch(maps, Nest.class);
    }


    public static List<Nest> queryNestByParent(String parentName, Connection connection){
        if (isUseCache()){
            DatabaseUniquenessConnectionWrapper connectionWrapper = getWrapperConnection(connection);
            String databaseName = connectionWrapper.getDatabaseName();
            List<Nest> list = nestCache.getOrDefault(databaseName, new ArrayList<>());
            return StreamUtils.filterList(list, ele -> parentName.equals(ele.getParentName()));
        }
        if (!init){
            init(connection);
        }
        String sql = ServiceUtils.parseTxt(QUERY_NEST_BY_PARENT_NAME, "#{", "}", str -> MapArgHandler.getString(parentName));
        log.info("[NestManager] -- run query sql: {}", sql);
        List<Map<String, Object>> maps;
        try {
            maps = SQLUtils.runJavaSelect(sql, connection, new HumpColumnConvertHandler());
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        return BeanUtil.toBeanBatch(maps, Nest.class);
    }


    public static List<Dict> queryAllDict(Connection connection){
        if (isUseCache()){
            DatabaseUniquenessConnectionWrapper connectionWrapper = getWrapperConnection(connection);
            String databaseName = connectionWrapper.getDatabaseName();
            return new ArrayList<>(dictCache.getOrDefault(databaseName, new ArrayList<>()));
        }
        if (!init){
            init(connection);
        }
        log.info("[NestManager] -- run query sql: {}", QUERY_ALL_DICT_SQL);
        List<Map<String, Object>> maps;
        try {
            maps = SQLUtils.runJavaSelect(QUERY_ALL_DICT_SQL, connection, new HumpColumnConvertHandler());
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        return BeanUtil.toBeanBatch(maps, Dict.class);
    }

    public static List<Dict> queryDictBySource(String sourceName, Connection connection){
        if (isUseCache()){
            DatabaseUniquenessConnectionWrapper connectionWrapper = getWrapperConnection(connection);
            String databaseName = connectionWrapper.getDatabaseName();
            List<Dict> list = dictCache.getOrDefault(databaseName, new ArrayList<>());
            return StreamUtils.filterList(list, ele -> sourceName.equals(ele.getSourceTableName()));
        }
        if (!init){
            init(connection);
        }
        String sql = ServiceUtils.parseTxt(QUERY_DICT_BY_SOURCE_NAME, "#{", "}", str -> MapArgHandler.getString(sourceName));
        log.info("[NestManager] -- run query sql: {}", sql);
        List<Map<String, Object>> maps;
        try {
            maps = SQLUtils.runJavaSelect(sql, connection, new HumpColumnConvertHandler());
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        return BeanUtil.toBeanBatch(maps, Dict.class);
    }
}

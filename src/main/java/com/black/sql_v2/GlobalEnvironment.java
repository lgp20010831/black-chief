package com.black.sql_v2;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.datasource.ProduceElementDataSourceDiscriminateManager;
import com.black.datasource.SqlControllerElementResolver;
import com.black.json.JsonParser;
import com.black.json.OversimplifyJsonParser;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.core.tools.BeanUtil;
import com.black.sql_v2.executor.SqlRunntimeExecutor;
import com.black.sql_v2.handler.SqlStatementHandler;
import com.black.sql_v2.listener.SqlListener;
import com.black.sql_v2.result.SqlResultHandler;
import com.black.sql_v2.sql_statement.JavaSqlStatementHandler;
import com.black.sql_v2.statement.SqlStatementBuilder;
import com.black.sql_v2.statement.delete.DeleteSqlStatementBuilder;
import com.black.sql_v2.statement.insert.InsertSqlStatementBuilder;
import com.black.sql_v2.statement.select.DefaultSelectSqlStatementBuilder;
import com.black.sql_v2.statement.select.SelectCountSqlStatementBuilder;
import com.black.sql_v2.statement.update.UpdateSqlStatementBuilder;
import com.black.sql_v2.utils.SqlV2Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

@Getter @Setter
public class GlobalEnvironment extends SqlSeqMetadata{


    private static final Map<String, DataSourceBuilder> dataSourceBuilderCache = new ConcurrentHashMap<>();

    private static GlobalEnvironment environment;

    public static DataSourceBuilder getDataSourceBuilder(String alias){
        return dataSourceBuilderCache.get(alias);
    }

    public static void registerDataSource(String alias, DataSourceBuilder dataSourceBuilder){
        dataSourceBuilderCache.put(alias, dataSourceBuilder);
    }

    private static final ReentrantLock LOCK = new ReentrantLock();

    public static GlobalEnvironment getInstance() {
        LOCK.lock();
        try {
            if (environment == null){
                environment = new GlobalEnvironment();
            }
            return environment;
        }finally {
            LOCK.unlock();
        }
    }

    private GlobalEnvironment(){
        initAttribute();
        ProduceElementDataSourceDiscriminateManager.registerResolver(new SqlControllerElementResolver());
    }

    // 初始化一些默认属性
    private void initAttribute(){
        log = LogFactory.getArrayLog();
        log.debug("[SQL] -- sql global environment initializing...");
        jsonParser = new OversimplifyJsonParser();
        dataSourceBuilder = new SpringDataSourceBuilder();
        convertHandler = new HumpColumnConvertHandler();
        defaultSelectSqlBuilder = new DefaultSelectSqlStatementBuilder();
        defaultInsertSqlBuilder = new InsertSqlStatementBuilder();
        defaultUpdateSqlBuilder = new UpdateSqlStatementBuilder();
        defaultDeleteSqlBuilder = new DeleteSqlStatementBuilder();
        defaultSelectCountSqlBuilder = new SelectCountSqlStatementBuilder();
        displayConfiguration = new StatementValueSetDisplayConfiguration();
        ChiefScanner scanner = ScannerManager.getScanner();

        for (Class<?> type : scanner.load("com.black.sql_v2.statement.select")) {
            if (SqlStatementBuilder.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                if (SelectCountSqlStatementBuilder.class.isAssignableFrom(type)){
                    continue;
                }
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                selectSqlBuilders.add((SqlStatementBuilder) instance);
            }
        }

        for (Class<?> type : scanner.load("com.black.sql_v2.handler")) {
            if (SqlStatementHandler.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                sqlStatementHandlers.add((SqlStatementHandler) instance);
            }
        }

        for (Class<?> type : scanner.load("com.black.sql_v2.executor")) {
            if (SqlRunntimeExecutor.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                sqlRunntimeExecutors.add((SqlRunntimeExecutor) instance);
            }
        }

        for (Class<?> type : scanner.load("com.black.sql_v2.listener")) {
            if (SqlListener.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                listeners.add((SqlListener) instance);
            }
        }

        for (Class<?> type : scanner.load("com.black.sql_v2.result")) {
            if (SqlResultHandler.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                resultHandlers.add((SqlResultHandler) instance);
            }
        }

        for (Class<?> type : scanner.load("com.black.sql_v2.sql_statement")) {
            if (JavaSqlStatementHandler.class.isAssignableFrom(type) && BeanUtil.isSolidClass(type)){
                Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
                log.debug("[SQL] -- init instance processor: [{}]", SqlV2Utils.getName(instance));
                printStringTextHandler(instance);
                javaSqlStatementHandlers.add((JavaSqlStatementHandler) instance);
            }
        }
        log.debug("[SQL] -- sql global environment initialization completed...");
    }

    private void printStringTextHandler(Object instance){
        if (instance instanceof AbstractStringSupporter){
            String prefix = ((AbstractStringSupporter) instance).getPrefix();
            log.trace("[SQL] -- {} --> prefix handler ---> [{}]",
                    prefix, BeanUtil.getPrimordialClass(instance).getSimpleName());
        }
    }

    //属性:
    private DataSourceBuilder dataSourceBuilder;

    private IoLog log;

    private JsonParser jsonParser;

    private int insertBatch = 2000;

    private AliasColumnConvertHandler convertHandler;

    private SqlStatementBuilder defaultSelectCountSqlBuilder;

    private SqlStatementBuilder defaultSelectSqlBuilder;

    private SqlStatementBuilder defaultInsertSqlBuilder;

    private SqlStatementBuilder defaultUpdateSqlBuilder;

    private SqlStatementBuilder defaultDeleteSqlBuilder;

    private StatementValueSetDisplayConfiguration displayConfiguration;

    private final LinkedBlockingQueue<SqlStatementBuilder> selectSqlBuilders = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<SqlStatementHandler> sqlStatementHandlers = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<SqlRunntimeExecutor> sqlRunntimeExecutors = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<SqlResultHandler> resultHandlers = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<SqlListener> listeners = new LinkedBlockingQueue<>();

    private final LinkedBlockingQueue<JavaSqlStatementHandler> javaSqlStatementHandlers = new LinkedBlockingQueue<>();
}

package com.black.log;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.sql_v2.Sql;
import com.black.utils.ServiceUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * @author 李桂鹏
 * @create 2023-06-06 10:43
 */
@SuppressWarnings("all") @Data
public class SerializeGlobalLogConfiguration {

    private static SerializeGlobalLogConfiguration configuration;

    public static synchronized SerializeGlobalLogConfiguration getInstance() {
        if (configuration == null){
            configuration = new SerializeGlobalLogConfiguration();
        }
        return configuration;
    }

    private String logInfoTableName = "v_log_record";

    private Class<? extends LogRecord> entity = LogInfo.class;

    private boolean autoCheckAndCreate = true;

    private String createTableSqlFilePath = "log_sql/build_log_table.sql";

    private IoLog logProxy;

    private String dataSourceAlias = Sql.DEFAULT_ALIAS;

    private Set<LogLevel> serializeLogLevels = new LinkedHashSet<>();

    private boolean serializeInAsync = true;

    private boolean print = true;

    private boolean serialze = true;

    private AliasColumnConvertHandler convertHandler;

    private LinkedBlockingQueue<Consumer<LogRecord>> recordCallback = new LinkedBlockingQueue<>();

    public SerializeGlobalLogConfiguration(){
        serializeLogLevels.add(LogLevel.INFO);
        serializeLogLevels.add(LogLevel.DEBUG);
        serializeLogLevels.add(LogLevel.ERROR);
        serializeLogLevels.add(LogLevel.TRACE);
        logProxy = LogFactory.getLog4j();
    }

    public void addRecordCallbacks(Consumer<LogRecord>... consumers){
        recordCallback.addAll(Arrays.asList(consumers));
    }

    public void removeLevel(LogLevel... logLevels){
        serializeLogLevels.removeIf(logLevel -> {
            return ServiceUtils.equalsAll(logLevel, logLevels);
        });
    }

    public void addLevels(LogLevel... logLevels){
        serializeLogLevels.addAll(Arrays.asList(logLevels));
    }
}

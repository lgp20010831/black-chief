package com.black.log;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.asyn.AsynGlobalExecutor;
import com.black.core.log.IoLog;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.ExceptionUtil;
import com.black.core.util.StringUtils;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.serialize.SerializeUtils;
import com.black.throwable.IOSException;
import com.black.utils.IdUtils;
import com.black.utils.ScriptRunner;
import com.black.utils.ServiceUtils;
import io.swagger.annotations.Api;
import lombok.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * @author 李桂鹏
 * @create 2023-06-06 11:05
 */
@SuppressWarnings("all")
public class LogHandler {

    private final SerializeGlobalLogConfiguration configuration;

    public LogHandler(@NonNull SerializeGlobalLogConfiguration configuration) {
        this.configuration = configuration;
    }

    public void print(Class<?> callClass, Method callMethod, LogLevel level, Throwable ex,
                      String msg, Object... params){
        IoLog logProxy = configuration.getLogProxy();
        String txt = createPrefix(callClass, callMethod) + msg;
        if (configuration.isPrint()){
            switch (level){
                case INFO:
                    logProxy.info(txt, params);
                    break;
                case DEBUG:
                    logProxy.debug(txt, params);
                    break;
                case ERROR:
                    logProxy.error(ex, txt, params);
                    break;
                case TRACE:
                    logProxy.trace(txt, params);
                    break;
            }
        }

        if (configuration.isSerialze()){
            if (configuration.isSerializeInAsync()){
                AsynGlobalExecutor.execute(() -> {
                    serialier0(callClass, callMethod, level, ex);
                });
            }else {
                serialier0(callClass, callMethod, level, ex);
            }
        }

    }

    protected String createPrefix(Class<?> callClass, Method callMethod){
        return StringUtils.letString("[", callClass.getSimpleName(), "(", callMethod.getName(), ")", "] => ");
    }

    protected void serialier0(Class<?> callClass, Method callMethod, LogLevel level, Throwable ex){
        try {
            SqlExecutor executor = Sql.opt(configuration.getDataSourceAlias());
            AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
            if (convertHandler != null){
                executor.getEnvironment().setConvertHandler(convertHandler);
            }

            if (configuration.isAutoCheckAndCreate()) {
                checkTable();
            }
            Class<? extends LogRecord> entity = configuration.getEntity();
            LogRecord logRecord = InstanceBeanManager.instance(entity, InstanceType.REFLEX_AND_BEAN_FACTORY);
            fillLogRecord(callClass, callMethod, level, ex, logRecord);
            LinkedBlockingQueue<Consumer<LogRecord>> queue = configuration.getRecordCallback();
            for (Consumer<LogRecord> consumer : queue) {
                consumer.accept(logRecord);
            }
            executor.insert(configuration.getLogInfoTableName(), SerializeUtils.serialize(logRecord));
        }catch (Throwable e){
            configuration.getLogProxy().error(e, "Exception in serializing log information");
        }
    }

    protected void checkTable(){
        String filePath = configuration.getCreateTableSqlFilePath();
        JHexByteArrayInputStream resource = new JHexByteArrayInputStream(ServiceUtils.getNonNullResource(filePath));
        Connection connection = Sql.opt(configuration.getDataSourceAlias()).getConnection();
        try {
            String source = resource.readNewString();
            source = ServiceUtils.parseTxt(source, "${", "}", flag -> {
                if ("tableName".equals(flag)){
                    return configuration.getLogInfoTableName();
                }
                return "";
            });
            Reader reader = wrapperSourceToReader(source);
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(reader);
        } catch (IOException e) {
            throw new IOSException(e);
        }finally {
            Sql.opt(configuration.getDataSourceAlias()).closeConnection();
        }
    }

    protected Reader wrapperSourceToReader(String source) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(source.getBytes());
        return new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    protected void fillLogRecord(Class<?> callClass, Method callMethod, LogLevel level, Throwable ex, LogRecord record){
        record.setModel(getModel(callClass, callMethod));
        record.setId(IdUtils.createId());
        record.setLevel(level.name());
        record.setControllerName(callClass.getSimpleName());
        record.setJavaMethod(callMethod.getName());
        HttpServletRequest request = getRequest();
        if (request != null){
            record.setUrl(request.getRequestURI());
            record.setRequestMethod(request.getMethod());
            record.setOperIp(HttpRequestUtil.getIpAddr(request));

        }
        record.setOperTime(ServiceUtils.now());
        if (ex != null){
            record.setStatus("1");
            record.setErrorMsg(ServiceUtils.getThrowableMessage(ex));
            record.setErrorStack(ExceptionUtil.getStackTraceInfo(ex));
        }else {
            record.setStatus("0");
        }
    }

    public static String getModel(Class<?> callClass, Method callMethod){
        Model annotation = callMethod.getAnnotation(Model.class);
        if (annotation == null){
            annotation = callClass.getAnnotation(Model.class);
        }
        if (annotation != null){
            return annotation.value();
        }
        Api api = callClass.getAnnotation(Api.class);
        return api == null ? null : api.tags().length == 0 ? null : api.tags()[0];
    }

    public static HttpServletRequest getRequest(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){
            return null;
        }
        return attributes.getRequest();
    }
}

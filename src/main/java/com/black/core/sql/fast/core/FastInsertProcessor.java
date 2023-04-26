package com.black.core.sql.fast.core;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.ImportPlatform;
import com.black.core.sql.annotation.Param;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.StatementSetConfigurationLocal;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.config.ConfigurationTreatment;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.inter.ExecuteResultResolver;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.result.ResultHandlerCollector;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.fast.annotation.FastInsert;
import com.black.core.sql.fast.config.FastInsertConfig;
import com.black.core.sql.run.RunSupport;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastInsertProcessor implements RunSupport {

    private Map<Method, FastInsertConfig> configMap = new ConcurrentHashMap<>();

    private Map<Method, ExecuteResultResolver> resultResolverCache = new ConcurrentHashMap<>();

    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(FastInsert.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        FastInsertConfig fastInsertConfig = configMap.computeIfAbsent(mw.getMethod(), m -> {
            FastInsert annotation = mw.getAnnotation(FastInsert.class);
            FastInsertConfig config = AnnotationUtils.loadAttribute(annotation, new FastInsertConfig(configuration, mw));
            config.setCw(cw);
            config.setMethodType(SQLMethodType.INSERT);
            ClassWrapper<?> wrapper = config.getCw();
            if(wrapper.inlayAnnotation(ImportPlatform.class)){
                wrapper = ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value());
            }
            return (FastInsertConfig) ConfigurationTreatment.treatmentConfig(config, wrapper);
        });

        ParameterWrapper parameter = mw.getSingleParameterByAnnotation(Param.class);
        Assert.notNull(parameter, "需要指定添加的核心数据, 标注@Param");
        //主要数据源
        List<Object> list = SQLUtils.wrapList(args[parameter.getIndex()]);
        Connection connection = ConnectionManagement.getConnection(configuration.getDataSourceAlias());
        String tableName = getTableName(mw, fastInsertConfig, args);
        SqlOutStatement sql = SqlWriter.insert(tableName);
        TableMetadata metadata = TableUtils.getTableMetadata(tableName, connection);
        Assert.notNull(metadata, "无法找到: " + tableName + " 数据源");
        for (String column : metadata.getColumnNameSet()) {
            sql.insertVariable(column, "?");
        }
        Map<String, Object> objectMap = MapArgHandler.parse(args, mw);
        synchronized (fastInsertConfig.getSqlSequences()){
            for (String sqlSeq : fastInsertConfig.getSqlSequences()) {
                sqlSeq = GlobalMapping.parseAndObtain(sqlSeq, true);
                SqlSequencesFactory.parseSeq(sql, sqlSeq, OperationType.INSERT, objectMap, metadata);
            }
        }
        sql.flush();
        final AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        String sqlTxt = sql.toString();
        Log log = configuration.getLog();
        if (log != null && log.isDebugEnabled()) {
            log.debug("==> fast insert sql: [" + sqlTxt + "]");
        }
        PreparedStatement statement = fastInsertConfig.isParseResult() ?
                connection.prepareStatement(sqlTxt, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sqlTxt);
        int batch = 0;
        int batchSize = fastInsertConfig.getBatchSize();
        StatementValueSetDisplayConfiguration setValueConfiguration = StatementSetConfigurationLocal.getSetValueConfiguration();
        for (Object obj : list) {
            Map<String, Object> json = SQLUtils.wrapMap(obj);
            for (SqlVariable variable : sql.getVariables()) {
                String alias = convertHandler.convertAlias(variable.getColumnName());
                Object val = json.get(alias);
                int type = metadata.getColumnMetadata(variable.getColumnName()).getType();
                SQLUtils.setStatementValue(statement, variable.getIndex(), val, type, setValueConfiguration);
            }
            statement.addBatch();
            if (++batch == batchSize){
                statement.executeBatch();
                statement.clearBatch();
                batch = 0;
                System.out.println("fast insert executebatch...");
            }
        }
        statement.executeBatch();
        try {

            if (fastInsertConfig.isParseResult()){
                ExecuteResultResolver resolver = resultResolverCache.computeIfAbsent(mw.getMethod(), m -> {
                    for (ExecuteResultResolver resultResolver : ResultHandlerCollector.getResultResolvers()) {
                        if (resultResolver.support(SQLMethodType.INSERT, mw)) {
                            return resultResolver;
                        }
                    }
                    return null;
                });
                if (resolver == null){
                    throw new SQLSException("指明了需要解析结果, 但是没有结果解析器支持解析当前方法, 目标方法: " + mw.getName());
                }
                ExecuteBody executeBody = new ExecuteBody(new StatementWrapper(statement));
                return resolver.doResolver(executeBody, fastInsertConfig, mw, false);
            }

            //不解析返回值类型, 总是返回空
            return null;
        }finally {
            SQLUtils.closeStatement(statement);
        }
    }

    public String getTableName(MethodWrapper mw, FastInsertConfig fastInsertConfig, Object[] args){
        String tableName = fastInsertConfig.getTableName();
        if (!StringUtils.hasText(tableName)){
            ParameterWrapper parameter = mw.getSingleParameterByAnnotation(TableName.class);
            Object arg;
            if (parameter == null || (arg = args[parameter.getIndex()]) == null){
                throw new IllegalStateException("无法找到操作的表名");
            }
            tableName = arg.toString();
        }
        return tableName;
    }
}

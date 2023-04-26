package com.black.core.sql.code.listener.rbi;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.ImportPlatform;
import com.black.core.sql.annotation.RemoveBeforeInsert;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.ConfigurationTreatment;
import com.black.core.sql.code.config.RemoveBeforeInsertConfig;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.factory.PacketFactory;
import com.black.core.sql.lock.LockType;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlValue;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.TableUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SQLListener
public class RemoveBeforeInsertListener implements GlobalSQLRunningListener {

    Map<Configuration, RemoveBeforeInsertConfig> configCache = new ConcurrentHashMap<>();

    Map<Method, Map<String, BlendObject>> blendCache = new ConcurrentHashMap<>();

    @Override
    public String postInsertSql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList) {
        MethodWrapper mw = configuration.getMethodWrapper();
        if (!mw.hasAnnotation(RemoveBeforeInsert.class) || configuration.getMethodType() != SQLMethodType.INSERT){
            return sql;
        }
        RemoveBeforeInsertConfig config = configCache.computeIfAbsent(configuration, m -> {
            RemoveBeforeInsert annotation = mw.getAnnotation(RemoveBeforeInsert.class);
            RemoveBeforeInsertConfig beforeInsertConfig = AnnotationUtils.loadAttribute(annotation, new RemoveBeforeInsertConfig(configuration));
            beforeInsertConfig.setMethodType(SQLMethodType.DELETE);
            ClassWrapper<?> wrapper = beforeInsertConfig.getCw();
            if(wrapper.inlayAnnotation(ImportPlatform.class)){
                wrapper = ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value());
            }
            try {
                return (RemoveBeforeInsertConfig) ConfigurationTreatment.treatmentConfig(beforeInsertConfig, wrapper);
            }finally {
                beforeInsertConfig.setMethodType(SQLMethodType.INSERT);
            }
        });


        String blendVoice = config.getBlendVoice();
        Map<String, BlendObject> blendObjectMap = blendCache.computeIfAbsent(mw.getMethod(), m -> {
            List<BlendObject> blendObjects = CharParser.parseBlend(blendVoice);
            return CharParser.toMap(blendObjects);
        });
        Set<String> judgmentBasis = new HashSet<>();
        if (StringUtils.hasText(blendVoice)){
            String tableName = config.getTableName();
            BlendObject object = blendObjectMap.get(tableName);
            if (object != null){
                judgmentBasis.addAll(object.getAttributes());
            }else {
                String foreignKeyColumnName = ((AppearanceConfiguration) configuration).getForeignKeyColumnName();
                if (foreignKeyColumnName != null){
                    judgmentBasis.add(foreignKeyColumnName);
                }
            }
        }else {
            if (configuration instanceof AppearanceConfiguration){
                String foreignKeyColumnName = ((AppearanceConfiguration) configuration).getForeignKeyColumnName();
                if (foreignKeyColumnName != null){
                    judgmentBasis.add(foreignKeyColumnName);
                }
            }else {
                //添加默认属性
                String primaryName = config.getPrimaryName();
                if (primaryName != null){
                    judgmentBasis.add(primaryName);
                }
            }
        }
        lock(config);
        doInvokeSql(judgmentBasis, config, valueGroupList);
        return sql;
    }

    private void doInvokeSql(Set<String> judgmentBasis, RemoveBeforeInsertConfig config, List<SqlValueGroup> valueGroupList){
        SqlOutStatement statement = SqlWriter.delete(config.getTableName());
        Map<String, List<String>> listMap = new HashMap<>();
        for (SqlValueGroup valueGroup : valueGroupList) {
            for (String judgmentBasi : judgmentBasis) {
                SqlValue value = valueGroup.findSingle(judgmentBasi, OperationType.INSERT);
                if (value == null){
                    throw new SQLSException("添加之前删除操作: 无法在添加的数据中找到依赖的字段:" + judgmentBasi);
                }
                Object valueValue = value.getValue();
                if (valueValue == null){
                    throw new SQLSException("添加之前删除操作: 依赖的条件数据不允许为空:" + judgmentBasi);
                }
                List<String> list = listMap.computeIfAbsent(judgmentBasi, jb -> new ArrayList<>());
                list.add(SQLUtils.getString(valueValue));
            }
        }
        listMap.forEach((c, l) -> {
            statement.writeIn(c, false, l);
        });

        ExecutePacket currentPacket = PacketFactory.getCurrentPacket();
        synchronized (config.getSqlSequences()){
            for (String sqlSeq : config.getSqlSequences()) {
                sqlSeq = GlobalMapping.parseAndObtain(sqlSeq, true);
                SqlSequencesFactory.parseSeq(statement, sqlSeq, OperationType.DELETE, currentPacket.getOriginalArgs(), config.getTableMetadata());
            }
        }

        statement.flush();
        String sql = statement.toString();
        Log log = config.getLog();
        if (log.isDebugEnabled()){
            log.debug("==> remove before insert:[" + sql + "]");
        }
        int i = SQLUtils.runSql(sql, config.getConnection());
        if (log.isDebugEnabled()){
            log.debug("<==> remove total:[" + i + "]");
        }
    }

    private void lock(RemoveBeforeInsertConfig config){
        TableUtils.lock(config.getConnection(), config.getTableName(), LockType.EXCLUSIVE);
    }
}

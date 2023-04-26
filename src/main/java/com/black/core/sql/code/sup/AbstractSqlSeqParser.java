package com.black.core.sql.code.sup;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.sup.impl.EqSeqParser;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.Av0;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import com.black.throwable.InterceptException;
import com.black.utils.ServiceUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSqlSeqParser implements SqlSeqParser{


    @Override
    public void doParse(String seq, SqlOutStatement statement, OperationType type, Map<String, Object> argMap, TableMetadata metadata) {
        seq = StringUtils.removeFrontSpace(seq);
        switch (type){
            case INSERT:
                insertParse(getSeq(seq), statement, argMap, metadata);
                break;
            case UPDATE:
                updateParse(getSeq(seq), statement, argMap, metadata);
                break;
            case SELECT:
            case DELETE:
                boolean andConnector = andConnector(seq);
                if (andConnector){
                    statement.and();
                }else {
                    statement.or();
                }
                boolean isWrite = queryParse(getSeq(seq), statement, argMap, metadata);
                if (!isWrite){
                    statement.removeLastOperation();
                }
                break;
        }
    }

    public static void main(String[] args) throws InterceptException {
        EqSeqParser parser = new EqSeqParser();
        String value = parser.processorValue("#{!map.startTime}", Av0.js("map", Av0.js("startTime", null)), null);
        System.out.println(value);
    }

    //#{!!xxx}  加一个!, 表示xxx存在时才成立否则抛出拦截异常, !!表示存在且不为空时成立,否则抛拦截异常
    protected String processorValue(String valueTxt, Map<String, Object> argMap, TableMetadata metadata) throws InterceptException {
        AtomicBoolean intercept = new AtomicBoolean(false);
        String result = ServiceUtils.parseTxt(valueTxt, "#{", "}", paramName -> {
            boolean limit = false;
            boolean forceLimit = false;
            paramName = StringUtils.removeFrontSpace(paramName);
            if (paramName.startsWith("!")) {
                limit = true;
                paramName = StringUtils.removeIfStartWith(paramName, "!");
            }

            if (limit && paramName.startsWith("!")) {
                forceLimit = true;
                paramName = StringUtils.removeIfStartWith(paramName, "!");
            }
            Object value = argMap;
            for (String param : paramName.split("\\.")) {
                if (value == null) {
                    if (forceLimit || limit) {
                        intercept.set(true);
                    }
                    return null;
                }

                if (value instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) value;
                    if (!map.containsKey(param)) {
                        if (limit) {
                            intercept.set(true);
                            return null;
                        }
                    } else {
                        value = map.get(param);
                        if (value == null && forceLimit) {
                            intercept.set(true);
                            return null;
                        }
                    }
                } else {
                    value = SetGetUtils.invokeGetMethod(param, value);
                    if (value == null && forceLimit) {
                        intercept.set(true);
                        return null;
                    }
                }

            }
            return value == null ? "null" : MapArgHandler.getString(value);
        });

        if (intercept.get()){
            throw new InterceptException();
        }
        return result;
    }


    public boolean queryParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata){
        String sql = MapArgHandler.parseSql(seq, argMap);
        statement.write(sql, OperationType.SELECT);
        return true;
    }

    public void insertParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata){}

    public void updateParse(String seq, SqlOutStatement statement, Map<String, Object> argMap, TableMetadata metadata){}

    protected boolean saveColumn(TableMetadata metadata, String columnName){
        if (metadata == null){
            return true;
        }
        return metadata.getColumnNameSet().contains(columnName);
    }

    public boolean andConnector(String seq){
        return !seq.startsWith("or");
    }

    public String getSeq(String seq){
        String space = StringUtils.removeFrontSpace(seq);
        space = StringUtils.removeIfStartWith(space, "and");
        space = StringUtils.removeIfStartWith(space, "or");
        return space;
    }

    public boolean existConnector(String seq){
        String space = StringUtils.removeFrontSpace(seq);
        return space.startsWith("and") || space.startsWith("or");
    }

}

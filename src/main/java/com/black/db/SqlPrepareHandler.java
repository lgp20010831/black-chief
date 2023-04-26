package com.black.db;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.util.Body;
import com.black.sql.JdbcSqlUtils;
import com.black.sql.Query;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class SqlPrepareHandler {

    private final DBGlobalConfiguration configuration;

    public SqlPrepareHandler(DBGlobalConfiguration configuration) {
        this.configuration = configuration;
    }

    public String prepareSql(String sql, Map<String, Object> env, Object... params){
        int before = ServiceUtils.PARSE_TXT_USE_MODEL;
        try {
            ServiceUtils.PARSE_TXT_USE_MODEL = 1;
            if (configuration.isOpenDBGlobalMapping()) {
                Map<String, String> mappingStatements = configuration.getGlobalMappingStatements();
                sql = ServiceUtils.parseTxt(sql, "%{", "}", txt -> {
                    String statement = mappingStatements.get(txt);
                    return statement == null ? "" : statement;
                });
            }
            if (configuration.isOpenChiefGlobalMapping()){
                sql = GlobalMapping.parseAndObtain(sql);
            }

            if (configuration.isOpenDichotomous()){
                sql = DBUtils.parseDichotomousSql(sql, env);
            }

            if (configuration.isOpenMapParamInjection()){
                Map<String, Object> targetEnv = new Body(env);
                if (configuration.isUseGlobalEnv()){
                    targetEnv.putAll(configuration.getGlobalEnv());
                }
                sql = ServiceUtils.parseTxt(sql, "#{", "}", key -> {
                    Object val = ServiceUtils.findValue(targetEnv, key);
                    return MapArgHandler.getString(val);
                });

                sql = ServiceUtils.parseTxt(sql, "^{", "}", key -> {
                    Object value = ServiceUtils.findValue(targetEnv, key);
                    return value == null ? "null" : JdbcSqlUtils.getEscapeString(value.toString());
                });
            }

            if (configuration.isUseJpaFictitiousStrategy()){
                Map<Object, Object> paramArrayMap = DBUtils.castParamArray(params);
                sql = Query.doParseSql(sql, paramArrayMap);
            }
        }finally {
            ServiceUtils.PARSE_TXT_USE_MODEL = before;
        }
        return sql;
    }
}

package com.black.sql_v2.handler;

import com.black.core.bean.TrustBeanCollector;
import com.black.core.json.Trust;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.serialize.SerializeUtils;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.utils.ServiceUtils;

import java.util.Map;

@SuppressWarnings("all")
public class SelectMapConditionHandler extends AbstractWhereConditionHandler {

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        boolean entity = !(param instanceof Map);
        //JSONObject conditionMap = JsonUtils.letJson(param);
        Map<String, Object> conditionMap = SerializeUtils.serialize(param);
        if (entity){
            ServiceUtils.filterNullValueMap(conditionMap);
        }
        JDBCEnvironmentLocal.setEnv(conditionMap);
        SqlV2Utils.putMapToStatement(conditionMap, statement);
        return statement;
    }

    @Override
    public boolean support(Object param) {
        if (param == null){
            return false;
        }
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(param);
        return param instanceof Map || TrustBeanCollector.existTrustBean(primordialClass)
                || AnnotationUtils.isPertain(primordialClass, Trust.class);
    }
}

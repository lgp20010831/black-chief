package com.black.xml;

import com.black.core.log.IoLog;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.factory.AgentLayer;
import com.black.core.spring.factory.AgentObject;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.xml.PrepareSource;
import com.black.result_set.ResultSetHandlerManager;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-08 14:03
 */
@SuppressWarnings("all")
public class XmlProxyInvokeHandler implements AgentLayer {

    private final XmlExecutor xmlExecutor;

    public XmlProxyInvokeHandler(XmlExecutor xmlExecutor) {
        this.xmlExecutor = xmlExecutor;
    }


    @Override
    public Object proxy(AgentObject layer) throws Throwable {
        IoLog log = xmlExecutor.getLog();
        Method method = layer.getProxyMethod();
        MethodWrapper mw = MethodWrapper.get(method);
        String id = method.getName();
        Object[] args = layer.getArgs();
        args = args == null ? new Object[0] : args;
        Map<Object, Object> indexMap = XmlUtils.castIndexMap(args);
        Map<String, Object> env = MapArgHandler.parse(args, mw);
        boolean select = xmlExecutor.isSelect(id);
        SQLMethodType methodType = select ? SQLMethodType.QUERY : SQLMethodType.UPDATE;
        try {
            String sql = xmlExecutor.invoke(id, env, indexMap);
            PrepareSource prepareSource = xmlExecutor.prepare();
            log.info("[XML] invoke {} sql ===> {}", select ? "query" : "update", sql);
            ResultSet resultSet = null;
            if (select){
                resultSet = SQLUtils.runQuery(sql, xmlExecutor.getConnection());
            }else {
                SQLUtils.executeSql(sql, xmlExecutor.getConnection());
            }
            return ResultSetHandlerManager.resolve(methodType, mw, resultSet, prepareSource);
        }finally {
            xmlExecutor.closeConnection();
        }

    }
}

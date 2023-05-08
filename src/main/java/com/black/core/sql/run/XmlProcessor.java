package com.black.core.sql.run;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.XmlMapper;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.MapperXmlApplicationContext;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlEngine;
import com.black.core.sql.xml.XmlManager;
import com.black.core.sql.xml.XmlUtils;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;

import java.sql.Connection;
import java.util.Map;

public class XmlProcessor extends SqlRunner implements RunSupport{
    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(XmlMapper.class) || mw.getAnnotationMap().size() == 0;
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        if (!XmlManager.isOpen()){
            throw new SQLSException("not support xml mapper");
        }else {
            XmlManager.init();
        }
        Connection connection = ConnectionManagement.getConnection(configuration.getDataSourceAlias());
        XmlMapper annotation = mw.getAnnotation(XmlMapper.class);
        String id = mw.getName();
        if (annotation != null){
            String value = annotation.value();
            if (StringUtils.hasText(value)){
                id = value;
            }
        }
        MapperXmlApplicationContext context = (MapperXmlApplicationContext) configuration.getApplicationContext();
        ElementWrapper xmlBind = context.findXmlBind(id);
        Assert.notNull(xmlBind, "not find bind mapper on method: " + mw.getName());
        Map<String, Object> argMap = MapArgHandler.parse(args, mw);
        ElementWrapper copy = xmlBind.createCopy();
        PrepareSource prepareSource = new PrepareSource(connection, configuration.getConvertHandler());
        String sql = XmlEngine.processorSql(copy, argMap, prepareSource);
        String compressSql = XmlUtils.removeLineFeed(sql);
        Log log = configuration.getLog();
        if (log != null && log.isDebugEnabled()) {
            log.debug("--> compress sql: [" + compressSql + "]");
        }
        return runSql(sql, "query".equals(xmlBind.getName()),
                connection,
                log,
                mw, configuration.getConvertHandler(), configuration);
    }
}

package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.*;

@Log4j2
public class ForXmlNodeHandler extends AbstractXmlNodeHandler{

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, GlobalSQLConfiguration configuration) {
        Map<String, Object> argMap = sqlSource.getArgMap();
        String list = getAssertNullAttri(ew, "target");
        Object value = MapArgHandler.getValue(argMap, list);

        if (value != null && (value.getClass().isArray() || value instanceof Collection)){
            processorCollection(SQLUtils.wrapList(value), ew, sqlSource, configuration);
        }else if (value instanceof Map){
            processorMap((Map<String, Object>) value, ew, sqlSource, configuration);
        }else {
            ew.clearContent();
        }
        return false;
    }

    protected void processorCollection(Collection<?> collection,
                                       ElementWrapper ew,
                                       XmlSqlSource sqlSource,
                                       GlobalSQLConfiguration configuration){
        String space = getAssertNullAttri(ew, "space", "");
        String item = getAssertNullAttri(ew, "key", "key");
        final String itemTopic = "#{" + item + "}";
        String tempTxt;
        StringJoiner joiner = new StringJoiner(space);
        for (Object obj : collection) {
            ElementWrapper copy = ew.createCopy();
            Map<String, Object> argMap = sqlSource.getArgMap();
            try {
                argMap.put(item, obj);
                processorChild(copy, sqlSource, configuration);
                tempTxt = copy.getStringValue();
                joiner.add(tempTxt.replace(itemTopic, SQLUtils.getString(obj)));
            }finally {
                argMap.remove(item);
            }
        }
        ew.clearContent();
        ew.setText(joiner.toString());
    }

    protected void processorMap(Map<String, Object> map,
                                ElementWrapper ew,
                                XmlSqlSource sqlSource,
                                GlobalSQLConfiguration configuration){
        String mapValue = getAssertNullAttri(ew, "value", "value");
        String mapKey = getAssertNullAttri(ew, "key", "key");
        String space = getAssertNullAttri(ew, "space", "");
        String check = getAssertNullAttri(ew, "check", "");
        final String itemTopic = "#{" + mapKey + "}";
        StringJoiner joiner = new StringJoiner(space);
        //验证 map 数据源字段
        List<TableMetadata> metadataList = new ArrayList<>();
        if (StringUtils.hasText(check)){
            Connection connection = ConnectionManagement.getConnection(configuration.getDataSourceAlias());
            for (String name : check.split(",")) {
                TableMetadata metadata = TableUtils.getTableMetadata(name, connection);
                if (metadata != null){
                    metadataList.add(metadata);
                }else {
                    log.warn("no table check: [{}]", name);
                }
            }
        }
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        final String valTopic = "#{" + mapValue + "}";
        String tempTxt;
        for (String k : map.keySet()) {
            Object v = map.get(k);
            if (!metadataList.isEmpty()){
                boolean save = false;
                String column = convertHandler.convertColumn(k);
                for (TableMetadata metadata : metadataList) {
                    if (metadata.getColumnNameSet().contains(column)) {
                        save = true;
                        break;
                    }
                }
                if (!save){
                    continue;
                }
                tempTxt = replaceMapFor(itemTopic, valTopic, ew, sqlSource, mapKey, column, mapValue, v, configuration);
            }else {
                tempTxt = replaceMapFor(itemTopic, valTopic, ew, sqlSource, mapKey, k, mapValue, v, configuration);
            }
            joiner.add(tempTxt);
        }
        ew.clearContent();
        ew.setText(joiner.toString());
    }

    protected String replaceMapFor(String keyItemTopic,
                                   String valueItemTopic,
                                   ElementWrapper ew,
                                   XmlSqlSource sqlSource,
                                   String mapKey,
                                   String keyValue,
                                   String mapValue,
                                   Object valueValue,
                                   GlobalSQLConfiguration configuration){
        ElementWrapper copy = ew.createCopy();
        Map<String, Object> argMap = sqlSource.getArgMap();
        try {
            argMap.put(mapKey, keyValue);
            argMap.put(mapValue, valueValue);
            processorChild(copy, sqlSource, configuration);
            String tempTxt = copy.getStringValue();
            tempTxt = tempTxt.replace(keyItemTopic, keyValue);
            tempTxt = tempTxt.replace(valueItemTopic, SQLUtils.getString(valueValue));
            return tempTxt;
        }finally {
            argMap.remove(mapKey);
            argMap.remove(mapValue);
        }
    }
}

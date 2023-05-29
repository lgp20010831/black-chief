package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.util.*;

@Log4j2 @SuppressWarnings("all")
public class ForXmlNodeHandler extends AbstractXmlNodeHandler{

    @Override
    public String getLabelName() {
        return "for";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("target", "space", "key", "value", "check", "prefix", "suffix", "index");
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        Map<String, Object> argMap = sqlSource.getArgMap();
        String list = getAssertNullAttri(ew, "target");
        Object value = ServiceUtils.getByExpression(argMap, list);

        if (value != null && (value.getClass().isArray() || value instanceof Collection)){
            processorCollection(SQLUtils.wrapList(value), ew, sqlSource, prepareSource);
        }else if (value instanceof Map){
            processorMap((Map<String, Object>) value, ew, sqlSource, prepareSource);
        }else {
            ew.clearContent();
        }
        return false;
    }

    protected void processorCollection(Collection<?> collection,
                                       ElementWrapper ew,
                                       XmlSqlSource sqlSource,
                                       PrepareSource prepareSource){
        String space = getAssertNullAttri(ew, "space", "");
        String prefix = getAssertNullAttri(ew, "prefix", "");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String item = getAssertNullAttri(ew, "key", "item");
        String index = getAssertNullAttri(ew, "index", "index");
        final String itemTopic = "#{" + item + "}";
        final String indexTopic = "#{" + index + "}";
        String tempTxt;
        StringJoiner joiner = Utils.isEmpty(collection) ? new StringJoiner("") :
                new StringJoiner(" " + space + " ", " " + prefix + " ", " " + suffix + " ");
        int i = 0;
        for (Object obj : collection) {
            ElementWrapper copy = ew.createCopy();
            Map<String, Object> argMap = sqlSource.getArgMap();
            try {
                argMap.put(item, obj);
                processorChild(copy, sqlSource, prepareSource);
                tempTxt = copy.getStringValue();
                String indexStr = String.valueOf(i++);
                tempTxt = tempTxt.replace(indexTopic, indexStr);
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
                                PrepareSource prepareSource){
        Map<String, Object> argMap = sqlSource.getArgMap();
        String mapValue = getAssertNullAttri(ew, "value", "val");
        String mapKey = getAssertNullAttri(ew, "key", "item");
        String prefix = getAssertNullAttri(ew, "prefix", "");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String space = getAssertNullAttri(ew, "space", "");
        String check = getAssertNullAttri(ew, "check", "");
        String index = getAssertNullAttri(ew, "index", "index");
        final String itemTopic = "#{" + mapKey + "}";
        final String indexTopic = "#{" + index + "}";
        StringJoiner joiner = Utils.isEmpty(map) ? new StringJoiner("") :
                new StringJoiner(" " + space + " ", " " + prefix + " ", " " + suffix + " ");        //验证 map 数据源字段
        List<TableMetadata> metadataList = new ArrayList<>();
        if (StringUtils.hasText(check)){
            Connection connection = prepareSource.getConnection();
            for (String name : check.split(",")) {
                if (name.startsWith("${")){
                    name = ServiceUtils.parseTxt(name, "${", "}", param -> {
                        return String.valueOf(ServiceUtils.getByExpression(argMap, param));
                    });
                }
                TableMetadata metadata = TableUtils.getTableMetadata(name, connection);
                if (metadata != null){
                    metadataList.add(metadata);
                }else {
                    log.warn("no table check: [{}]", name);
                }
            }
        }
        AliasColumnConvertHandler convertHandler = prepareSource.getConvertHandler();
        final String valTopic = "#{" + mapValue + "}";
        String tempTxt;
        int i = 0;
        for (String k : map.keySet()) {
            Object v = map.get(k);
            String indexStr = String.valueOf(i++);
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
                tempTxt = replaceMapFor(itemTopic, valTopic, ew, sqlSource, mapKey, column, mapValue, v, prepareSource);
            }else {
                tempTxt = replaceMapFor(itemTopic, valTopic, ew, sqlSource, mapKey, k, mapValue, v, prepareSource);
            }
            tempTxt = tempTxt.replace(indexTopic, indexStr);
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
                                   PrepareSource prepareSource){
        ElementWrapper copy = ew.createCopy();
        Map<String, Object> argMap = sqlSource.getArgMap();
        try {
            argMap.put(mapKey, keyValue);
            argMap.put(mapValue, valueValue);
            processorChild(copy, sqlSource, prepareSource);
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

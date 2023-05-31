package com.black.core.sql.xml.impl;

import com.alibaba.fastjson.JSONObject;
import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.json.JsonUtils;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.ServiceUtils;

import java.util.*;

@SuppressWarnings("all")
public class FortableNodeHandler extends AbstractXmlNodeHandler{

    @Override
    public String getLabelName() {
        return "fortable";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("target", "table", "prefix", "suffix", "space", "blend");
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        return false;
    }

    @Override
    protected void processorSqlSource(ElementWrapper ew, XmlSqlSource sqlSource, PrepareSource prepareSource) {
        ew.clearContent();
        Map<String, Object> argMap = sqlSource.getArgMap();
        String targetName = getAssertNullAttri(ew, "target");
        String prefix = getAssertNullAttri(ew, "prefix", "where");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String space = getAssertNullAttri(ew, "space", "and");
        String blend = getAssertNullAttri(ew, "blend", "");
        String tableName = getAssertNullAttri(ew, "table");
        AliasColumnConvertHandler convertHandler = prepareSource.getConvertHandler();
        TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, prepareSource.getConnection());
        Object val = ServiceUtils.getByExpression(argMap, targetName);
        if (val instanceof Collection){
            throw new IllegalStateException("fortable only support handler map");
        }

        JSONObject target = JsonUtils.letJson(val);
        if (target.isEmpty()){
            return;
        }

        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        List<BlendObject> blendObjects = CharParser.parseBlend(blend);
        Map<String, String> blendMap = castBlendToMap(blendObjects, convertHandler);
        StringJoiner joiner = new StringJoiner(appendBlankSpace(space), appendBlankSpace(prefix), appendBlankSpace(suffix));
        for (String key : target.keySet()) {
            Object value = target.get(key);
            String operator = blendMap.get(key);
            String column = convertHandler.convertColumn(key);
            if (!columnNameSet.contains(column)){
                continue;
            }
            String condition = getSqlSeq(column, value, operator);
            joiner.add(condition);
        }
        ew.setText(joiner.toString());
    }

    public static String appendBlankSpace(String str){
        return str == null ? " " : " " + str + " ";
    }

    public static String getSqlSeq(String column, Object value, String operator){
        if (value == null){
            return column + " is null";
        }

        String valStr = MapArgHandler.getString(value);
        if (operator == null){
            return column + " = " + valStr;
        }

        switch (operator){
            case "like":
            case "Like":
            case "LIKE":
                return column + " like %" + value + "%";
            case ">":
                return column + " > " + valStr;
            case ">=":
                return column + " >= " + valStr;
            case "<":
                return column + " < " + valStr;
            case "<=":
                return column + " <= " + valStr;
            case "<>":
                return column + "<> " + valStr;
            case "in":
            case "IN":
            case "In":
                return column + " in " + valStr;
            case "not in":
            case "Not in":
            case "Not In":
            case "not In":
            case "NOT IN":
                return column + " not in " + valStr;
            default:
                throw new IllegalStateException("not support blend syntax: " + operator);
        }
    }
    
    public static Map<String, String> castBlendToMap(List<BlendObject> blendObjects, AliasColumnConvertHandler convertHandler){
        Map<String, String> map = new LinkedHashMap<>();
        for (BlendObject object : blendObjects) {
            for (String attribute : object.getAttributes()) {
                attribute = convertHandler == null ? attribute : convertHandler.convertAlias(attribute);
                map.put(attribute, object.getName());
            }
        }
        return map;
    }
}

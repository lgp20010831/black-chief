package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.utils.ServiceUtils;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2 @SuppressWarnings("all")
public class ForXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "for";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("target", "space", "key", "value", "prefix", "suffix", "index");
    }

    @Override
    protected boolean resolve(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        Map<String, Object> argMap = labelTextCarrier.getArgMap();
        String list = getAssertNullAttri(ew, "target");
        Object value = ServiceUtils.getByExpression(argMap, list);

        if (value != null && (value.getClass().isArray() || value instanceof Collection)){
            processorCollection(SQLUtils.wrapList(value), ew, labelTextCarrier, engine);
        }else if (value instanceof Map){
            processorMap((Map<String, Object>) value, ew, labelTextCarrier, engine);
        }else {
            ew.clearContent();
        }
        return false;
    }



    protected void processorCollection(Collection<?> collection,
                                       ElementWrapper ew,
                                       LabelTextCarrier labelTextCarrier,
                                       XmlResolveEngine engine){
        String space = getAssertNullAttri(ew, "space", "");
        String prefix = getAssertNullAttri(ew, "prefix", "");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String item = getAssertNullAttri(ew, "key", "item");
        String index = getAssertNullAttri(ew, "index", "index");
        final String itemTopic = "#{" + item + "}";
        final String indexTopic = "#{" + index + "}";
        String tempTxt;
        StringJoiner joiner = new StringJoiner(space, prefix, suffix);
        int i = 0;
        for (Object obj : collection) {
            ElementWrapper copy = ew.createCopy();
            Map<String, Object> argMap = labelTextCarrier.getArgMap();
            try {
                argMap.put(item, obj);
                processorChild(labelTextCarrier, copy, engine);
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
                                LabelTextCarrier labelTextCarrier,
                                XmlResolveEngine engine){
        String mapValue = getAssertNullAttri(ew, "value", "val");
        String mapKey = getAssertNullAttri(ew, "key", "item");
        String prefix = getAssertNullAttri(ew, "prefix", "");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String space = getAssertNullAttri(ew, "space", "");
        String check = getAssertNullAttri(ew, "check", "");
        String index = getAssertNullAttri(ew, "index", "index");
        final String itemTopic = "#{" + mapKey + "}";
        final String indexTopic = "#{" + index + "}";
        StringJoiner joiner = new StringJoiner(space, prefix, suffix);

        final String valTopic = "#{" + mapValue + "}";
        String tempTxt;
        int i = 0;
        for (String k : map.keySet()) {
            Object v = map.get(k);
            String indexStr = String.valueOf(i++);
            tempTxt = replaceMapFor(itemTopic, valTopic, ew, labelTextCarrier, mapKey, k, mapValue, v, engine);
            tempTxt = tempTxt.replace(indexTopic, indexStr);
            joiner.add(tempTxt);
        }
        ew.clearContent();
        ew.setText(joiner.toString());
    }

    protected String replaceMapFor(String keyItemTopic,
                                   String valueItemTopic,
                                   ElementWrapper ew,
                                   LabelTextCarrier labelTextCarrier,
                                   String mapKey,
                                   String keyValue,
                                   String mapValue,
                                   Object valueValue,
                                   XmlResolveEngine engine){
        ElementWrapper copy = ew.createCopy();
        Map<String, Object> argMap = labelTextCarrier.getArgMap();
        try {
            argMap.put(mapKey, keyValue);
            argMap.put(mapValue, valueValue);
            processorChild(labelTextCarrier, copy, engine);
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

package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.code.condition.ConditionSelector;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.sql.xml.XmlUtils;
import com.black.core.util.StringUtils;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IfXmlNodeHandler extends AbstractXmlNodeHandler {

    private final IoLog log = LogFactory.getArrayLog();

    @Override
    public String getLabelName() {
        return "if";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("test", "then");
    }

    @Override
    protected boolean resolve(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        String attrVal = getAssertNullAttri(ew, "test");
        String then = getAssertNullAttri(ew, "then", "");
        Map<String, Object> argMap = labelTextCarrier.getArgMap();
        attrVal = XmlUtils.prepareConditionItem(attrVal);
        log.debug("[IF] --> execute conditional expression: {}", attrVal);
        if (!ConditionSelector.excCondition(attrVal, argMap)) {
            //sqlSource.setSql(sqlSource.getSql().replace(value, ""));
            //当表达式返回 false 则要走 else 分支
            ElementWrapper wrapper = ew.getByName("else");
            if (wrapper != null){
                //处理 else 分支的子分支
                processorChild(labelTextCarrier, ew, engine);

                //收集 else 的文本内容
                String txt = wrapper.getStringValue();
                ew.clearContent();
                ew.setText(txt);
            }else {
                ew.clearContent();
            }
            return false;
        }else {
            if (StringUtils.hasText(then)){
                ew.clearContent();
                ew.setText(then);
            }
        }
        return true;
    }
}

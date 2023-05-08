package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.condition.ConditionSelector;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IfXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "if";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("test");
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        String attrVal = getAssertNullAttri(ew, "test");
        Map<String, Object> argMap = sqlSource.getArgMap();
        String value = ew.getStringValue();
        attrVal = attrVal.replace("and", "&&");
        if (!ConditionSelector.excCondition(attrVal, argMap)) {
            //sqlSource.setSql(sqlSource.getSql().replace(value, ""));
            //当表达式返回 false 则要走 else 分支
            ElementWrapper wrapper = ew.getByName("else");
            if (wrapper != null){
                //处理 else 分支的子分支
                processorChild(wrapper, sqlSource, prepareSource);

                //收集 else 的文本内容
                String txt = wrapper.getStringValue();
                ew.clearContent();
                ew.setText(txt);
            }else {
                ew.clearContent();
            }
            return false;
        }
        return true;
    }
}

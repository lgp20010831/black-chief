package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.code.condition.ConditionSelector;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.sql.xml.XmlUtils;
import com.black.core.util.StringUtils;
import com.black.core.util.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        String attrVal = getAssertNullAttri(ew, "test");
        String then = getAssertNullAttri(ew, "then", "");
        Map<String, Object> argMap = sqlSource.getArgMap();
        attrVal = XmlUtils.prepareConditionItem(attrVal);
        log.debug("[IF] --> execute conditional expression: {}", attrVal);
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
        }else {
            if (StringUtils.hasText(then)){
                ew.clearContent();
                ew.setText(then);
            }
        }
        return true;
    }


    public static void main(String[] args) {

    }
}

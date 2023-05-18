package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.condition.ConditionSelector;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.sql.xml.XmlUtils;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;
import org.apache.el.lang.ELArithmetic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-11 15:51
 */
@SuppressWarnings("all")
public class SwitchNodeHandler extends AbstractXmlNodeHandler{

    @Override
    public String getLabelName() {
        return "switch";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("target, case[test,then]", "def[then]", "prefix", "suffix");
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        String prefix = getAssertNullAttri(ew, "prefix", "");
        String suffix = getAssertNullAttri(ew, "suffix", "");
        String target = getAssertNullAttri(ew, "target");
        Map<String, Object> argMap = sqlSource.getArgMap();
        String targetValue = String.valueOf(ServiceUtils.getByExpression(argMap, target));

        //拿到所有 csaes
        List<ElementWrapper> cases = ew.getsByName("case");
        //拿到 def
        ElementWrapper def = ew.getByName("def");
        String append = null;
        boolean invoke = false;
        for (ElementWrapper caseWrapper : cases) {
            String test = getAssertNullAttri(caseWrapper, "test").trim();
            if (!StringUtils.hasText(test)){
                caseWrapper.clearContent();
                continue;
            }
            if (!targetValue.equals(test)) {
                caseWrapper.clearContent();
                continue;
            }

            invoke = true;
            processorChild(caseWrapper, sqlSource, prepareSource);
            //如果表达式通过
            String then = caseWrapper.getAttrVal("then");
            if (!StringUtils.hasText(then)){
                then = caseWrapper.getStringValue();
            }
            caseWrapper.clearContent();
            caseWrapper.setText(prefix + " " + then + " " + suffix);
            append = then;
            break;
        }
        if (!invoke){
            //执行 default
            if (def != null){

                processorChild(def, sqlSource, prepareSource);
                String then = def.getAttrVal("then");
                if (!StringUtils.hasText(then)){
                    append = def.getStringValue();
                }
                def.clearContent();
                def.setText(prefix + " " + then + " " + suffix);
                append = then;
            }
        }else {
            if (def != null){
                def.clearContent();
            }
        }

        return false;
    }
}

package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.util.Assert;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public abstract class AbstractXmlNodeHandler implements XmlNodeHandler {

    public String getLabelName(){
        return "unsupport";
    }

    public List<String> getAttributeNames(){
        return new ArrayList<>();
    }

    @Override
    public void doHandler(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        if (resolve(labelTextCarrier, ew, engine)) {
            processorChild(labelTextCarrier, ew, engine);
        }

        processorSqlSource(labelTextCarrier, ew, engine);
    }

    protected abstract boolean resolve(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine);

    protected String getAssertNullAttri(ElementWrapper ew, String name){
        return getAssertNullAttri(ew, name, null);
    }

    protected String getAssertNullAttri(ElementWrapper ew, String name, String defaultValue){
        String attrVal = ew.getAttrVal(name);
        if (attrVal == null){
            if (defaultValue == null){
                throw new IllegalStateException("attribute: [" + name + "] of node[" + ew.getName() + "], is not allow null");
            }
            attrVal = defaultValue;
        }
        return attrVal;
    }


    protected void processorChild(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine){
        Map<String, List<ElementWrapper>> elements = ew.getElements();
        for (String name : elements.keySet()) {
            List<ElementWrapper> wrapperList = elements.get(name);
            for (ElementWrapper wrapper : wrapperList) {
                XmlNodeHandler handler = engine.getHandler(wrapper.getName());
                Assert.notNull(handler, "unknown xml node is [" + wrapper.getName() + "]");
                handler.doHandler(labelTextCarrier, wrapper, engine);
            }
        }
    }

    protected void processorSqlSource(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine){

    }
}

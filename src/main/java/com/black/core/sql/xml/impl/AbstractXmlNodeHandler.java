package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlEngine;
import com.black.core.sql.xml.XmlNodeHandler;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.core.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractXmlNodeHandler implements XmlNodeHandler {

    public String getLabelName(){
        return "unsupport";
    }

    public List<String> getAttributeNames(){
        return new ArrayList<>();
    }

    @Override
    public void doHandler(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        if (resolve(sqlSource, ew, prepareSource)) {
            processorChild(ew, sqlSource, prepareSource);
        }

        processorSqlSource(ew, sqlSource, prepareSource);
    }

    protected abstract boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource);

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


    protected void processorChild(ElementWrapper ew, XmlSqlSource sqlSource, PrepareSource prepareSource){
        Map<String, List<ElementWrapper>> elements = ew.getElements();
        for (String name : elements.keySet()) {
            List<ElementWrapper> wrapperList = elements.get(name);
            for (ElementWrapper wrapper : wrapperList) {
                XmlNodeHandler handler = XmlEngine.getHandler(wrapper.getName());
                Assert.notNull(handler, "unknown xml node is [" + wrapper.getName() + "]");
                handler.doHandler(sqlSource, wrapper, prepareSource);
            }
        }
    }

    protected void processorSqlSource(ElementWrapper ew, XmlSqlSource sqlSource, PrepareSource prepareSource){

    }
}

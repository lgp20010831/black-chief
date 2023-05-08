package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.EscapeProcessor;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;

import java.util.Arrays;
import java.util.List;


public class QueryXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "query";
    }

    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("id");
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        return true;
    }

    @Override
    protected void processorSqlSource(ElementWrapper ew, XmlSqlSource sqlSource, PrepareSource prepareSource) {
        sqlSource.setSql(EscapeProcessor.escape(ew.getStringValue()));
    }
}

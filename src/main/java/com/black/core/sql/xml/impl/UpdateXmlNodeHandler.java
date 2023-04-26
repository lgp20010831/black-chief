package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.EscapeProcessor;
import com.black.core.sql.xml.XmlSqlSource;

public class UpdateXmlNodeHandler extends AbstractXmlNodeHandler{

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, GlobalSQLConfiguration configuration) {
        return true;
    }

    @Override
    protected void processorSqlSource(ElementWrapper ew, XmlSqlSource sqlSource, GlobalSQLConfiguration configuration) {
        sqlSource.setSql(EscapeProcessor.escape(ew.getStringValue()));
    }
}

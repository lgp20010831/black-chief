package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.XmlSqlSource;

/** 该 esle 实际不会做任何事 */
public class ElseXmlNodeHandler extends AbstractXmlNodeHandler {
    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, GlobalSQLConfiguration configuration) {
        ew.clearContent();
        return false;
    }
}

package com.black.core.sql.xml.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;

import java.util.Arrays;
import java.util.List;

/** 该 esle 实际不会做任何事 */
public class ElseXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "else";
    }

    @Override
    protected boolean resolve(XmlSqlSource sqlSource, ElementWrapper ew, PrepareSource prepareSource) {
        ew.clearContent();
        return false;
    }
}

package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;

/** 该 esle 实际不会做任何事 */
public class ElseXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "else";
    }

    @Override
    protected boolean resolve(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        ew.clearContent();
        return false;
    }

}

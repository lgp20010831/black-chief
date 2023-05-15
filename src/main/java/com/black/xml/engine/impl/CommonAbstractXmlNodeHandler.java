package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.xml.EscapeProcessor;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;




public abstract class CommonAbstractXmlNodeHandler extends AbstractXmlNodeHandler {

    @Override
    protected boolean resolve(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        return true;
    }

    @Override
    protected void processorSqlSource(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine) {
        labelTextCarrier.setText(EscapeProcessor.escape(ew.getStringValue()));
    }

}

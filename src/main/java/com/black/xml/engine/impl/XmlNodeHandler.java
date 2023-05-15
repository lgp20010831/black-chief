package com.black.xml.engine.impl;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlSqlSource;
import com.black.xml.engine.LabelTextCarrier;
import com.black.xml.engine.XmlResolveEngine;

public interface XmlNodeHandler {

    void doHandler(LabelTextCarrier labelTextCarrier, ElementWrapper ew, XmlResolveEngine engine);
}

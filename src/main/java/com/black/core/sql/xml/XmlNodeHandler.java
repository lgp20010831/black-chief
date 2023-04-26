package com.black.core.sql.xml;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;

public interface XmlNodeHandler {

    void doHandler(XmlSqlSource sqlSource, ElementWrapper ew, GlobalSQLConfiguration configuration);
}

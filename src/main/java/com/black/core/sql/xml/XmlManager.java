package com.black.core.sql.xml;

import com.black.core.sql.xml.impl.*;

public class XmlManager {

    private static volatile boolean init = false;

    public static boolean isOpen(){
        try {
            Class.forName("org.dom4j.Element");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    public static synchronized void init(){
        if (!init){
            init = true;
            XmlEngine.addHandler("query", new QueryXmlNodeHandler());
            XmlEngine.addHandler("for", new ForXmlNodeHandler());
            XmlEngine.addHandler("if", new IfXmlNodeHandler());
            XmlEngine.addHandler("update", new UpdateXmlNodeHandler());
            XmlEngine.addHandler("else", new ElseXmlNodeHandler());
        }
    }
}

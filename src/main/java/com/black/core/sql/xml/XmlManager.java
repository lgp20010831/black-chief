package com.black.core.sql.xml;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.xml.impl.*;
import com.black.utils.ServiceUtils;

import java.util.List;

@SuppressWarnings("all")
public class XmlManager {

    private static IoLog log = LogFactory.getArrayLog();

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
            List<AbstractXmlNodeHandler> handlers = ServiceUtils.scanAndLoad("com.black.core.sql.xml.impl", AbstractXmlNodeHandler.class);
            for (AbstractXmlNodeHandler handler : handlers) {
                String labelName = handler.getLabelName();
                List<String> attributeNames = handler.getAttributeNames();
                XmlEngine.addHandler(labelName, handler);
                log.trace("[XML MANAGER INIT] register handler support: {} --- attributes: {}", labelName, attributeNames);
            }

        }
    }
}

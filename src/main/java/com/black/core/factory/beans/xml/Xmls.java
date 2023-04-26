package com.black.core.factory.beans.xml;

import com.black.utils.ServiceUtils;

import java.io.InputStream;

public class Xmls {

    public static XmlWrapper create(String msg){
        return new XmlWrapper(new XmlMessage(msg));
    }

    public static XmlWrapper find(String path){
        return read(ServiceUtils.getResource(path));
    }

    public static XmlWrapper read(InputStream inputStream){
        return new XmlWrapper(new XmlMessage(inputStream));
    }

}

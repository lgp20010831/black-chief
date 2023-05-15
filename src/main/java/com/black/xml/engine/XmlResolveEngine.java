package com.black.xml.engine;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.xml.XmlEngine;

import com.black.core.sql.xml.XmlUtils;
import com.black.core.util.Assert;
import com.black.ftl.CompressionStrategy;
import com.black.utils.ServiceUtils;
import com.black.xml.engine.impl.AbstractXmlNodeHandler;
import com.black.xml.engine.impl.XmlNodeHandler;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-05-13 15:55
 */
@SuppressWarnings("all") @Log4j2 @Data
public class XmlResolveEngine {

    private final Map<String, XmlNodeHandler> handlerCache = new ConcurrentHashMap<>();

    private boolean compressText = false;

    private CompressionStrategy compressionStrategy = CompressionStrategy.compressSpaces;

    public Map<String, XmlNodeHandler> getHandlerCache() {
        return handlerCache;
    }

    public void addHandler(String nodeName, XmlNodeHandler nodeHandler){
        handlerCache.put(nodeName, nodeHandler);
    }

    public XmlNodeHandler getHandler(String nodeName){
        return getHandlerCache().get(nodeName);
    }

    public XmlResolveEngine(){
        init();
    }

    private void init(){
        List<AbstractXmlNodeHandler> handlers = ServiceUtils.scanAndLoad("com.black.xml.engine.impl", AbstractXmlNodeHandler.class);
        for (AbstractXmlNodeHandler handler : handlers) {
            String labelName = handler.getLabelName();
            List<String> attributeNames = handler.getAttributeNames();
            addHandler(labelName, handler);
            log.trace("[XML MANAGER INIT] register handler support: {} --- attributes: {}", labelName, attributeNames);
        }
    }

    public String resolve(ElementWrapper wrapper, Map<String, Object> env){
        env = env == null ? new LinkedHashMap<>() : env;
        LabelTextCarrier carrier = new LabelTextCarrier();
        carrier.setArgMap(env);
        XmlNodeHandler handler = getHandler(wrapper.getName());
        Assert.notNull(handler, "unknown xml node is [" + wrapper.getName() + "]");
        handler.doHandler(carrier, wrapper, this);
        String text = carrier.getText();
        if (compressText){
            if (compressionStrategy == CompressionStrategy.lostWrap){
                text = XmlUtils.compressSql(text);
            }else {
                text = XmlUtils.compressSpaces(text);
            }

        }
        return text;
    }
}

package com.black.core.factory.beans.xml;


import com.black.core.query.Wrapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

/***
 * 根据路径寻找
 * /xx/xx/xx 路径
 * //xxx 快速定位
 */
public class XmlWrapper implements Wrapper<Document> {


    protected Document document;
    protected final SAXReader reader = new SAXReader();
    protected final SAXWriter writer = new SAXWriter();
    protected final ElementWrapper rootElement;
    private final XmlMessage message;

    public XmlWrapper(XmlMessage message){
        this.message = message;
        try {
            InputStream stream = message.getStream();
            if (stream != null){
                document = reader.read(stream);
            }else {
                document = reader.read(new ByteArrayInputStream(message.getMessage().getBytes()));
            }
            rootElement = new ElementWrapper(document.getRootElement());
        } catch (DocumentException e) {
            throw new XmlsExceptions(e);
        }
    }

    public Document getDocument() {
        return document;
    }

    public ElementWrapper getRootElement() {
        return rootElement;
    }

    public InputStream getInputStream(){
        return new ByteArrayInputStream(asXML().getBytes());
    }

    public String asXML(){
        return document.asXML();
    }

    @Override
    public Document get() {
        return document;
    }
}

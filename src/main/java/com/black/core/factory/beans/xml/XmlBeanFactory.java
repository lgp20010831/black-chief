package com.black.core.factory.beans.xml;

import com.black.core.factory.beans.BeanFactoryUnsupportException;
import com.black.core.factory.beans.config.AnnotationConfigurationBeanFactory;
import com.black.core.spring.instance.InstanceConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.io.InputStream;

public class XmlBeanFactory extends AnnotationConfigurationBeanFactory {

    private ResourceReader reader;

    public XmlBeanFactory() {
    }

    @InstanceConstructor
    public XmlBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
    }

    @Override
    public Object get(Object param) {
        if (param instanceof Class<?>){
            return super.get(param);
        }
        if(param instanceof XmlMessage){
           return getXmlWrapper((XmlMessage) param);
        }

        if (param instanceof InputStream){
            return getXmlWrapper(new XmlMessage((InputStream) param));
        }

        if (param instanceof String){
           return parsePath((String) param);
        }
        throw new BeanFactoryUnsupportException("XML bean factory support param is a path or xmlMessage");
    }

    protected XmlWrapper parsePath(String path){
        if (reader == null){
            reader = new ClassLoaderResourceReader();
        }
        return getXmlWrapper(new XmlMessage(reader.reader(path)));
    }

    protected XmlWrapper getXmlWrapper(XmlMessage xmlMessage){
        return new XmlWrapper(xmlMessage);
    }

    public void setReader(ResourceReader reader) {
        this.reader = reader;
    }
}

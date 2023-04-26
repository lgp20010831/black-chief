package com.black.core.factory.beans.xml;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.BeanFactorysException;
import com.black.core.factory.beans.component.BeanFactoryHolder;

public class XmlFactoryUtils {

    public static XmlWrapper get(Object param){
        BeanFactory factory = BeanFactoryHolder.getFactory();
        if (factory != null){
            if (factory instanceof XmlBeanFactory){
                Object result = factory.get(param);
                if (result instanceof XmlWrapper){
                    return (XmlWrapper) result;
                }
                throw new BeanFactorysException("The returned value result cannot " +
                        "be parsed into an XML encapsulated object");
            }
        }
        throw new BeanFactorysException("The factory object cannot be empty or is not an XML factory");
    }

}

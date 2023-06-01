package com.black.xml.servlet;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.util.Assert;

/**
 * @author 李桂鹏
 * @create 2023-05-31 13:45
 */
@SuppressWarnings("all")
public interface ElementWrapperHandler {

    default String getAttribute(ElementWrapper elementWrapper, String name){
        return getAttribute(elementWrapper, name, null);
    }


    default String getAttribute(ElementWrapper elementWrapper, String name, String df){
        String attrVal = elementWrapper.getAttrVal(name);
        attrVal = attrVal == null ? df : attrVal;
        return Assert.nonNull(attrVal, "attrVal of " + name + " is null");
    }

}

package com.black.ftl;

import com.black.xml.engine.impl.CommonAbstractXmlNodeHandler;

import java.util.Arrays;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-13 16:18
 */
@SuppressWarnings("all")
public class ModelXmlHandler extends CommonAbstractXmlNodeHandler {

    @Override
    public String getLabelName() {
        return "model";
    }


    @Override
    public List<String> getAttributeNames() {
        return Arrays.asList("id");
    }
}

package com.black.core.mybatis.source;

import com.black.core.config.ApplicationConfigurationReader;

public class AttributeParser {

    private final ApplicationConfigurationReader ymlConfigurationHandler;

    public AttributeParser(ApplicationConfigurationReader ymlConfigurationHandler) {
        this.ymlConfigurationHandler = ymlConfigurationHandler;
    }

    public String getReallyValue(String key){
        if (key.startsWith("${")){

            if (!key.endsWith("}")){
                throw new IllegalStateException("attribute: " + key + " 缺少结束符: }");
            }
            String attributeName = key.substring(2, key.length() - 1);
            if (ymlConfigurationHandler != null){
                return ymlConfigurationHandler.selectAttribute(attributeName);
            }
        }else {
            return key;
        }
        return null;
    }
}

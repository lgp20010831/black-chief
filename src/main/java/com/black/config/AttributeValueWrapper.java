package com.black.config;

import com.black.core.chain.GroupKeys;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter @Getter
public class AttributeValueWrapper {

    Map<String, AttributeValue> attributeValueMap;


    Map<GroupKeys, AttributeValue> globalCache;

}

package com.black.core.sql.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class XmlSqlSource {

    private String sql = "";

    private Map<String, Object> argMap;


}

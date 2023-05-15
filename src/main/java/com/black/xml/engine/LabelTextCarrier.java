package com.black.xml.engine;

import lombok.Data;

import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-13 16:00
 */
@SuppressWarnings("all") @Data
public class LabelTextCarrier {

    private String text = "";

    private Map<String, Object> argMap;
}

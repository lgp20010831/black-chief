package com.black.xml.servlet;

/**
 * @author 李桂鹏
 * @create 2023-06-08 10:41
 */
@SuppressWarnings("all")
public enum ParamPart {

    RequestParam("@RequestParam"),
    RequestBody("@RequestBody"),
    RequestPart("@RequestPart");

    String annName;

    ParamPart(String annName) {
        this.annName = annName;
    }

    public String getAnnName() {
        return annName;
    }
}

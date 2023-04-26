package com.black.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class Response {

    private String version;

    private int code;

    private String status;

    private Map<String, String> headers;

    private String message;

}

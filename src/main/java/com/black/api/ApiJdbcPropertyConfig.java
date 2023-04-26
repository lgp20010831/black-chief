package com.black.api;

import lombok.Data;

@Data
public class ApiJdbcPropertyConfig {

    String response;

    String request;

    String[] httpHeaders;

    String remark;

    boolean hide;
}

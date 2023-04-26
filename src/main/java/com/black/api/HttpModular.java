package com.black.api;

import lombok.Data;

import java.util.List;

@Data
public class HttpModular {

    //模块备注
    String modularRemark;

    //排序
    Integer sort;

    //模块下的所有接口
    List<HttpMethod> methods;
}

package com.black.core.log.record;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author 李桂鹏
 * @create 2023-05-19 11:31
 */
@SuppressWarnings("all")
public class LogInfo {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("模块")
    private String model;

    @ApiModelProperty("请求地址")
    private String url;

    @ApiModelProperty("请求方式")
    private String requestMethod;

    @ApiModelProperty("java 方法名称")
    private String javaMethod;

    @ApiModelProperty("控制器名称")
    private String controllerName;

    @ApiModelProperty("操作人员")
    private String operName;

    @ApiModelProperty("主机 ip")
    private String operIp;

    @ApiModelProperty("请求参数")
    private String operParam;

    @ApiModelProperty("返回参数")
    private String jsonResult;

    @ApiModelProperty("操作状态（0正常 1异常）")
    private String status;

    @ApiModelProperty("错误消息")
    private String errorMsg;

    @ApiModelProperty("操作时间")
    private String operTime;

    @ApiModelProperty("异常栈")
    private String errorStack;

}

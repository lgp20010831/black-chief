package com.black.log;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author 李桂鹏
 * @create 2023-05-19 11:31
 */
@SuppressWarnings("all")
public class LogInfo implements LogRecord {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("日志等级")
    private String level;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getJavaMethod() {
        return javaMethod;
    }

    public void setJavaMethod(String javaMethod) {
        this.javaMethod = javaMethod;
    }

    public String getControllerName() {
        return controllerName;
    }

    public void setControllerName(String controllerName) {
        this.controllerName = controllerName;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public String getOperParam() {
        return operParam;
    }

    public void setOperParam(String operParam) {
        this.operParam = operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getOperTime() {
        return operTime;
    }

    public void setOperTime(String operTime) {
        this.operTime = operTime;
    }

    public String getErrorStack() {
        return errorStack;
    }

    public void setErrorStack(String errorStack) {
        this.errorStack = errorStack;
    }

    @Override
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String getLevel() {
        return level;
    }
}

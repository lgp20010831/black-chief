package com.black.log;

/**
 * @author 李桂鹏
 * @create 2023-06-06 10:45
 */
@SuppressWarnings("all")
public interface LogRecord {

    default void setId(String id){

    }

    default String getId(){
        return null;
    }

    default void setLevel(String level){

    }

    default String getLevel(){
        return null;
    }

    default void setModel(String id){

    }

    default String getModel(){
        return null;
    }

    default String getUrl() {
        return null;
    }

    default void setUrl(String url) {

    }

    default String getRequestMethod() {
        return null;
    }

    default void setRequestMethod(String requestMethod) {

    }

    default String getJavaMethod() {
        return null;
    }

    default void setJavaMethod(String javaMethod) {

    }

    default String getControllerName() {
        return null;
    }

    default void setControllerName(String controllerName) {

    }

    default String getOperName() {
        return null;
    }

    default void setOperName(String operName) {

    }

    default String getOperIp() {
        return null;
    }

    default void setOperIp(String operIp) {

    }

    default String getOperParam() {
        return null;
    }

    default void setOperParam(String operParam) {

    }

    default String getJsonResult() {
        return null;
    }

    default void setJsonResult(String jsonResult) {

    }

    default String getStatus() {
        return null;
    }

    default void setStatus(String status) {

    }

    default String getErrorMsg() {
        return null;
    }

    default void setErrorMsg(String errorMsg) {

    }

    default String getOperTime() {
        return null;
    }

    default void setOperTime(String operTime) {

    }

    default String getErrorStack() {
        return null;
    }

    default void setErrorStack(String errorStack) {

    }
}

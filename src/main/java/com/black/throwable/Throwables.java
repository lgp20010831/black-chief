package com.black.throwable;

import lombok.Setter;

public class Throwables {

    public static RuntimeExceptionSelector runtime(String msg){
        return runtime(msg, null);
    }

    public static RuntimeExceptionSelector runtime(Throwable cause){
        return runtime(null, cause);
    }

    public static RuntimeExceptionSelector runtime(String msg, Throwable cause){
        RuntimeExceptionSelector runtimeExceptionSelector = new RuntimeExceptionSelector();
        runtimeExceptionSelector.setCause(cause);
        runtimeExceptionSelector.setMsg(msg);
        return runtimeExceptionSelector;
    }

    @Setter
    public static class RuntimeExceptionSelector {
        private String msg;
        private Throwable cause;

        public String getMsg() {
            return msg == null ? "" : msg;
        }

        public Object chief(){
            throw new ChiefRuntimeException(getMsg(), cause);
        }

        public Object state(){
            throw new StateException(getMsg(), cause);
        }

        public Object parse(){
            throw new ParseException(getMsg(), cause);
        }

        public Object abort(){
            throw new AbortException(getMsg(), cause);
        }

        public Object noValue(){
            throw new NoValueException(getMsg(), cause);
        }
    }

    /** runtime 总运行异常 */
    static class ChiefRuntimeException extends RuntimeException{
        public ChiefRuntimeException() {
        }

        public ChiefRuntimeException(String message) {
            super(message);
        }

        public ChiefRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public ChiefRuntimeException(Throwable cause) {
            super(cause);
        }
    }

    /* 状态异常 */
    static class StateException extends ChiefRuntimeException{
        public StateException() {
        }

        public StateException(String message) {
            super(message);
        }

        public StateException(String message, Throwable cause) {
            super(message, cause);
        }

        public StateException(Throwable cause) {
            super(cause);
        }
    }

    /** 解析异常 */
    static class ParseException extends ChiefRuntimeException {
        public ParseException() {
        }

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParseException(Throwable cause) {
            super(cause);
        }
    }

    /** 中止异常 */
    static class AbortException extends ChiefRuntimeException{
        public AbortException() {
        }

        public AbortException(String message) {
            super(message);
        }

        public AbortException(String message, Throwable cause) {
            super(message, cause);
        }

        public AbortException(Throwable cause) {
            super(cause);
        }
    }

    /* 找不到值异常 */
    static class NoValueException extends ChiefRuntimeException{
        public NoValueException() {
        }

        public NoValueException(String message) {
            super(message);
        }

        public NoValueException(String message, Throwable cause) {
            super(message, cause);
        }

        public NoValueException(Throwable cause) {
            super(cause);
        }
    }
}

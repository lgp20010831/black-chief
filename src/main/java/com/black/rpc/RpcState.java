package com.black.rpc;

public enum RpcState {

    //服务器响应成功
    OK(1),
    //服务器发生错误
    ERROR(-1),
    //没有要请求的接口方法
    NO_METHOD(11),
    //无法解析请求, 请求不合法
    REQUEST_ERROR(17);


    int state;
    RpcState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public static RpcState typeOf(int i){
        switch (i){
            case 1:
                return RpcState.OK;
            case -1:
                return RpcState.ERROR;
            case 11:
                return NO_METHOD;
            default:
                throw new IllegalStateException("ill type: " + i);
        }
    }
}

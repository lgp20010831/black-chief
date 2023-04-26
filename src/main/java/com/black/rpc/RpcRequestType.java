package com.black.rpc;

public enum RpcRequestType {

    //获取一个接口方法的凭证
    //正常的调用
    COMMON(1), JSON(2);
    int type;

    RpcRequestType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static RpcRequestType typeOf(int i){
        switch (i){
            case 1:
                return RpcRequestType.COMMON;
            case 2:
                return RpcRequestType.JSON;
                default:
                throw new IllegalStateException("ill type: " + i);
        }
    }
}

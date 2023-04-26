package com.black.core.data;

import java.io.Serializable;

public enum DataStatus implements Serializable {

    CREATED,          //创建完成
    WAIT,             //等待队列中
    LOOP,             //循环中
    IN_HAND,          //处理中
    HAND_RECYCLED,    //处理后待回收
    NO_HAND_RECYCLED, //无处理待回收
    ABANDON   //丢弃
}

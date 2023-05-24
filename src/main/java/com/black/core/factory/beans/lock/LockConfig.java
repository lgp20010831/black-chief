package com.black.core.factory.beans.lock;

import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-05-24 13:40
 */
@SuppressWarnings("all")
@Data
public class LockConfig {

    int group;

    int share;

    //是否公平
    boolean fair;
}

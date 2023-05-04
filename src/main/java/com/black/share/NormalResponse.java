package com.black.share;

import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:10
 */
@SuppressWarnings("all") @Data
public class NormalResponse extends AbstractResponse{

    private final Object result;

    public NormalResponse(Object result) {
        this.result = result;
    }
}

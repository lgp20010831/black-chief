package com.black.share;

import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:11
 */
@SuppressWarnings("all") @Data
public class ThrowableResponse extends AbstractResponse{

    private final Throwable throwable;

    public ThrowableResponse(Throwable throwable) {
        this.throwable = throwable;
    }
}

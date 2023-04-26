package com.black.core.work.w1;


import lombok.Getter;

// 任务中断异常
public class WorkBreakException extends Exception {

    @Getter
    Object key;

    public WorkBreakException(Object key) {
        this.key = key;
    }

    public WorkBreakException(){}


    public WorkBreakException(Throwable cause) {
        super(cause);
    }
}

package com.black.throwable;

import lombok.NonNull;
import org.apache.poi.ss.formula.functions.T;

/**
 * @author 李桂鹏
 * @create 2023-06-01 14:30
 */
@SuppressWarnings("all")
public class ExceptionWrapperExcetion extends RuntimeException {

    protected final Throwable targetException;


    protected ExceptionWrapperExcetion(@NonNull Throwable targetException) {
        this.targetException = targetException;
    }

    public Throwable getTargetException() {
        return targetException;
    }

}

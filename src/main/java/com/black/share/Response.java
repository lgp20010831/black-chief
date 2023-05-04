package com.black.share;

import java.io.Serializable;

/**
 * @author 李桂鹏
 * @create 2023-05-04 10:48
 */
@SuppressWarnings("all")
public interface Response extends Serializable {

    String getResponseId();

    void setResponseId(String responseId);
}

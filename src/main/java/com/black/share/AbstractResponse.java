package com.black.share;

/**
 * @author 李桂鹏
 * @create 2023-05-04 10:49
 */
@SuppressWarnings("all")
public abstract class AbstractResponse implements Response{

    protected String responseId;

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    @Override
    public String getResponseId() {
        return responseId;
    }
}

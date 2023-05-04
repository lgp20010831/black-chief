package com.black.share;

/**
 * @author shkstart
 * @create 2023-05-04 10:43
 */
@SuppressWarnings("all")
public class AbstractRequest implements Request{

    protected String requestId;

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }


}

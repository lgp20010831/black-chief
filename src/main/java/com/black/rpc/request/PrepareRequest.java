package com.black.rpc.request;

import com.black.rpc.response.AbstractPrepare;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrepareRequest extends AbstractPrepare {

    private int type;

    private String requestId;

    private String methodName;
}

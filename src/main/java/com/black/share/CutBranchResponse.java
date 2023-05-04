package com.black.share;

import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-05-04 16:01
 */
@SuppressWarnings("all") @Data
public class CutBranchResponse extends AbstractResponse{

    private final String host;

    private final int port;

    public CutBranchResponse(String host, int port) {
        this.host = host;
        this.port = port;
    }

}

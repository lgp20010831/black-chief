package com.black.share;

import lombok.Data;

/**
 * @author 李桂鹏
 * @create 2023-05-04 14:52
 */
@SuppressWarnings("all") @Data
public class CulitivateBranchResponse extends AbstractResponse{

    private final Object process;

    private final int port;

    public CulitivateBranchResponse(Object process, int port) {
        this.process = process;
        this.port = port;
    }


}

package com.black.treaty.rtp;

import com.black.nio.group.NioType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RtpConfig {

    private int bindPort = 2000;

    private String bindHost = "0.0.0.0";

    private NioType nioType = NioType.CHIEF;

    private int IoThreadNum = 20;

}

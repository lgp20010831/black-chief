package com.black.treaty;

import com.black.nio.group.NioType;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TreatyConfig {

    private int bindPort = 2000;

    private String bindHost = "0.0.0.0";

    private NioType nioType = NioType.CHIEF;

    private int IoThreadNum = 20;

    private TreatyCustomHandler treatyCustomHandler;

    private boolean closeWhenError = false;

    private IoLog log = LogFactory.getArrayLog();
}

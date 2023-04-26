package com.black.core.aop.servlet.flow;

import com.black.JsonBean;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FlowProxyMatedata extends JsonBean {

    //谁代理我的
    FlowProxyMatedata proxyForMe;

    //当前代理服务器地址
    String address;
}

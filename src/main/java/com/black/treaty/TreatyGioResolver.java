package com.black.treaty;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.GioContext;
import com.black.nio.group.GioResolver;
import com.black.core.log.IoLog;
import com.black.utils.ServiceUtils;

import java.io.IOException;

public class TreatyGioResolver implements GioResolver {

    private final BaseTreatyNetworkScheduler baseTreatyNetworkScheduler;

    public TreatyGioResolver(BaseTreatyNetworkScheduler baseTreatyNetworkScheduler) {
        this.baseTreatyNetworkScheduler = baseTreatyNetworkScheduler;
    }

    @Override
    public void acceptCompleted(GioContext context, JHexByteArrayOutputStream out) {
        GioResolver.super.acceptCompleted(context, out);
        baseTreatyNetworkScheduler.registerClient(context);
    }

    @Override
    public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
        TreatyConfig config = baseTreatyNetworkScheduler.getConfig();
        IoLog log = config.getLog();
        String remoteAddress = context.remoteAddress();
        TreatyClient treatyClient = baseTreatyNetworkScheduler.findAddressClient(remoteAddress);
        if (treatyClient == null){
            log.error("[TREATY] 找不到连接得客户端: {}", remoteAddress);
            return;
        }
        TreatyCustomHandler customHandler = config.getTreatyCustomHandler();
        if (customHandler != null){
            log.trace("[TREATY] 当前协议处理器: {} ==> {} | {}", customHandler, remoteAddress, treatyClient.getClientId());
            try {
                customHandler.handle(treatyClient, bytes, out);
            } catch (Throwable e) {
                log.error("[TREATY] 协议处理器处理异常: {} ==> {} <== {}", customHandler,
                        treatyClient.getClientId(), ServiceUtils.getThrowableMessage(e, "系统异常"));
                if (config.isCloseWhenError()){
                    context.shutdown();
                }
            }
        }else {
            log.error("[TREATY] 没有定义协议处理器");
        }
    }

    @Override
    public void close(GioContext context) {
        baseTreatyNetworkScheduler.lost(context);
        GioResolver.super.close(context);
    }
}

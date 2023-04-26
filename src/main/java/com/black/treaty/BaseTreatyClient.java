package com.black.treaty;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.GioContext;
import com.black.utils.IdUtils;

public class BaseTreatyClient implements TreatyClient{

    private final GioContext gioContext;

    private final String clientId;

    public BaseTreatyClient(GioContext gioContext) {
        this.gioContext = gioContext;
        clientId = IdUtils.createShort8Id();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void write(Object source) {
        gioContext.write(source);
    }

    @Override
    public void flush() {
        gioContext.flush();
    }

    @Override
    public void writeAndFlush(Object source) {
        gioContext.writeAndFlush(source);
    }

    @Override
    public void shutdown() {
        gioContext.shutdown();
    }

    @Override
    public JHexByteArrayOutputStream getOutputStream() {
        return gioContext.getOutputStream();
    }

    @Override
    public String bindAddress() {
        return gioContext.bindAddress();
    }

    @Override
    public String remoteAddress() {
        return gioContext.remoteAddress();
    }


}

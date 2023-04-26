package com.black.udp;

import com.black.core.log.IoLog;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;

public class UdpStableReadThread extends UdpReadThread{


    public UdpStableReadThread(UdpStableSocket socket) {
        super(socket);
    }

    @Override
    protected void doResolveBytes(UdpCallback callback, UdpInputStream inputStream) {
        IoLog log = socket.getLog();
        SocketAddress remoteAddress = inputStream.getAddress();
        UdpStableSocket stableSocket = (UdpStableSocket) socket;
        Map<SocketAddress, Map<String, StablePackage>> packageCache = stableSocket.getPackageCache();
        try {

            int flag = inputStream.readInt();
            if (flag == UdpStableSocket.CONFIRM_FLAG){
                //是确认包
                UdpConfirmPackage confirmPackage = (UdpConfirmPackage) inputStream.readHexJavaObject();
                String id = confirmPackage.getId();
                log.info("rece confirm pack: {} from address:{}", id, remoteAddress);
                stableSocket.removePackage(remoteAddress, id);
                return;
            }

            if (flag == UdpStableSocket.STABLE_FLAG){
                //是需要确认的数据包
                StablePackage stablePackage = (StablePackage) inputStream.readHexJavaObject();
                String id = stablePackage.getId();
                byte[] bytes = stablePackage.getBytes();
                stableSocket.sendConfirmPackage(id, remoteAddress);
                inputStream = new UdpInputStream(bytes, remoteAddress, stableSocket);
            }
            //非确认包, 重置输入流
            inputStream.reset();
        } catch (IOException e) {
            //ignore
            try {
                inputStream.reset();
            } catch (IOException ex) {}
        }
        try {
            callback.read(inputStream, socket);
        } catch (Throwable e) {
            try {
                callback.receThrowable(e, socket, remoteAddress);
            } catch (Throwable ex) {
                log.info("handle read throwable append new throwable: {}", IoUtils.getThrowableMessage(ex));
            }
        }
    }
}

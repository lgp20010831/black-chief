package com.black.out_resolve.impl;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.utils.IoUtils;


public class StringRackResolver extends AbstractJHexOutputStreamResolver{

    private final IoLog log = LogFactory.getArrayLog();

    @Override
    protected boolean accurateSupport(Object rack) {
        return rack instanceof String;
    }

    @Override
    protected void resolveJHex(JHexByteArrayOutputStream outputStream, Object rack, Object value) throws Throwable {
        String rackStr = (String) rack;
        if ("utf".equalsIgnoreCase(rackStr)){
            outputStream.writeUTF(getStringValue(value));

        }else if ("byte".equalsIgnoreCase(rackStr)){
            outputStream.write(IoUtils.getBytes(value));

        }else if ("jhex".equalsIgnoreCase(rackStr)){
            outputStream.writeHexString(getStringValue(value));

        }else if ("java".equalsIgnoreCase(rackStr)){
            outputStream.writeHexJavaObject(value);

        }else {
            log.info("ill string rack: {}", rackStr);
        }
    }
}

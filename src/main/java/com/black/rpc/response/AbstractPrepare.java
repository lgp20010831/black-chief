package com.black.rpc.response;

import com.black.JsonBean;
import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.rpc.Prepare;
import com.black.utils.IoUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter @Setter
public abstract class AbstractPrepare extends JsonBean implements Prepare {
    protected int bodySize;

    protected DataByteBufferArrayInputStream bodyInput;

    @Override
    public int bodyBufferSize() throws IOException {
        return bodyInput.available();
    }

    @Override
    public void appendBody(byte[] body) throws IOException {
        byte[] readBytes = IoUtils.readBytes(bodyInput);
        byte[] buffer = new byte[body.length + readBytes.length];
        System.arraycopy(readBytes, 0, buffer, 0, readBytes.length);
        System.arraycopy(body, 0, buffer, readBytes.length, body.length);
        bodyInput = new DataByteBufferArrayInputStream(buffer);
    }

}

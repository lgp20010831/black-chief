package com.black.io.in;

import com.black.function.Function;
import com.black.core.util.Assert;
import com.black.utils.IoUtils;
import com.black.utils.JHex;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataByteBufferArrayInputStream extends DataInputStream {

    protected ByteArrayInputStream bais;

    public DataByteBufferArrayInputStream(byte[] buffer){
        super(null);
        bais = new ByteArrayInputStream(buffer);
        in = bais;
    }

    public DataByteBufferArrayInputStream(InputStream in) throws IOException {
        super(in);
        byte[] readBytes = IoUtils.readBytes(in);
        bais = new ByteArrayInputStream(readBytes);
        in = bais;
    }

    public int readAllHexInt() throws IOException {
        return readMultipleInt(available());
    }

    public String readAllHexString() throws IOException {
        return readMultipleHexString(available());
    }

    public int readMultipleInt(int size) throws IOException {
        String hexString = readMultipleHexString(size);
        return Integer.parseInt(hexString, 16);
    }

    public String[] readMultipleHexArray(int size) throws IOException {
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = readHexSting();
        }
        return array;
    }

    public String readMultipleHexString(int size) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(readHexSting());
        }
        return builder.toString();
    }

    public String readHexSting() throws IOException {
        int read = read();
        if (read == -1){
            throw new ReadEndUnableParseException(" read to the end");
        }
        return JHex.castStringToDigits((byte) read);
    }

    public int readHex() throws IOException {
        int read = read();
        if (read == -1){
            throw new ReadEndUnableParseException(" read to the end");
        }
        return JHex.castInt(JHex.castStringToDigits((byte) read));
    }

    public List<JHexByteArrayInputStream> unpacking(Function<Byte, Boolean> isHead,
                                                    Function<Byte, Boolean> isTail) throws IOException{
        return unpacking(isHead, isTail, false);
    }

    //分包
    public List<JHexByteArrayInputStream> unpacking(Function<Byte, Boolean> isHead,
                                                          Function<Byte, Boolean> isTail,
                                                          boolean containFlag) throws IOException{
        //当已经找到 head 还没有找到 tail 时为true
        boolean write = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<JHexByteArrayInputStream> result = new ArrayList<>();
        for (;;){
            int read = read();
            if (read == -1)
                break;

            if (!write){
                Boolean head;
                try {
                    head = isHead.apply((byte) read);
                } catch (Throwable e) {
                    throw new IOException(e);
                }
                Assert.notNull(head, "function result is must not null");
                write = head;
                if (containFlag && write){
                    out.write(read);
                }
            }else {
                Boolean tail;
                try {
                    tail = isTail.apply((byte) read);
                } catch (Throwable e) {
                    throw new IOException(e);
                }
                Assert.notNull(tail, "function result is must not null");
                write = !tail;
                if (write || containFlag){
                    out.write(read);
                }

                if (tail){
                    JHexByteArrayInputStream in = new JHexByteArrayInputStream(out.toByteArray());
                    result.add(in);
                    out.reset();
                }
            }
        }
        return result;
    }

    public String readUnrestrictedUtf() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (;;){
            try {
                String utf = readUTF();
                builder.append(utf);
            }catch (EOFException e){
                break;
            }
        }
        return builder.toString();
    }


}

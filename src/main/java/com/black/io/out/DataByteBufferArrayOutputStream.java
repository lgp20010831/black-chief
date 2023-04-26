package com.black.io.out;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.core.util.Assert;
import com.black.utils.JHex;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataByteBufferArrayOutputStream extends DataOutputStream {

    private ByteArrayOutputStream baos;

    private OutputStream writeOut;

    public DataByteBufferArrayOutputStream(){
        this(null);
    }

    public DataByteBufferArrayOutputStream(OutputStream writeOut) {
        super(null);
        baos = new ByteArrayOutputStream(256);
        out = baos;
        this.writeOut = writeOut;
    }

    public DataByteBufferArrayInputStream getInputStream(){
        return new DataByteBufferArrayInputStream(toByteArray());
    }

    public void writeHexString(String hexString) throws IOException {
        byte[] bytes = JHex.decode(hexString);
        write(bytes);
    }

    public void writeHex(int hex) throws IOException {
        String hexString = Integer.toHexString(hex);
        int len = hexString.length();
        if ((len & 0x01) != 0) {
            hexString = "0" + hexString;
        }
        writeHexString(hexString);
    }

    //写入指定字节数的int
    public void writeHex(int hex, int size) throws IOException {
        Assert.trueThrows(size > 4, "int max size is 4, but now is " + size);
        String int16 = JHex.limitInt16(hex, size * 2);
        writeHexString(int16);
    }

    //写入一个完整int 四个字节
    public void writeFullHex(int hex) throws IOException {
        String int16 = JHex.limitInt16(hex);
        writeHexString(int16);
    }

    public static int getUtfBytesLen(Object str){
        return str == null ? 0 : getUtfBytesLen(str.toString());
    }

    public static int getUtfBytesLen(String str){
        int length = str.length();
        int utflen = 0;
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen + 2;
    }

    public void writeUnrestrictedUtf(String str) throws IOException {
        int length = str.length();
        int utflen = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
            builder.append(c);
            if (utflen == 65532 || utflen == 65533 || utflen == 65534 || utflen == 65535){
                writeUTF(builder.toString());
                builder.delete(0, builder.length());
                utflen = 0;
            }
        }
        if(utflen > 0){
            writeUTF(builder.toString());
        }
    }

    public byte[] toByteArray(){
        return baos.toByteArray();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        if (writeOut != null){
            writeOut.write(toByteArray());
            writeOut.flush();
            baos.reset();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (writeOut != null){
            writeOut.close();
        }
    }

    public void reset(){
        baos.reset();
    }
}

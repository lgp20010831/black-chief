package com.black.io.in;

import com.black.function.Function;
import com.black.io.out.BinaryFile;
import com.black.io.out.BinaryPartElement;
import com.black.io.out.BinaryUtils;
import com.black.core.util.Assert;
import com.black.core.util.IoUtil;
import com.black.utils.JHex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JHexByteArrayInputStream extends DataByteBufferArrayInputStream{

    public JHexByteArrayInputStream(byte[] buffer) {
        super(buffer);
    }

    public JHexByteArrayInputStream(InputStream in) throws IOException {
        super(new byte[0]);
        this.in = in;
        bais = null;
    }

    public static final JHexByteArrayInputStream EMPTY_STREAM =  new JHexByteArrayInputStream(new byte[0]);

    public String readNewString() throws IOException {
        return new String(readAll());
    }

    public byte[] readAllBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte b;
        while ((b = (byte) read()) != -1){
            outputStream.write(b);
        }
        return outputStream.toByteArray();
    }

    public byte[] readAll() throws IOException {
        byte[] bytes = new byte[available()];
        read(bytes);
        return bytes;
    }

    //读一个完整的int值
    public int readHexFullInt() throws IOException {
        return readHex(4);
    }

    //读一个int值, 根据size 决定字节数
    public int readHex(int size) throws IOException {
        Assert.trueThrows(size > 4, "int max size is 4, but now is " + size);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String hexSting = readHexSting();
            builder.append(hexSting);
        }
        return Integer.parseInt(builder.toString(), 16);
    }

    public String readChineseHex() throws IOException {
        return JHex.castHexToChinese(readHexSting());
    }

    public String readAllChineseHex() throws IOException {
        return JHex.castHexToChinese(readAllHexString());
    }

    public String readMultipleChineseHex(int size) throws IOException {
        return JHex.castHexToChinese(readMultipleHexString(size));
    }

    public Object readHexJavaObject() throws IOException {
        int len = readInt();
        String hexString = readMultipleHexString(len);
        byte[] decode = JHex.decode(hexString);
        return IoUtil.serialize(decode);
    }

    public String readHexObjectString(int size) throws IOException {
        String hexString = readMultipleHexString(size);
        return JHex.decodeToString(hexString);
    }

    @Override
    public int readAllHexInt() throws IOException {
        return super.readAllHexInt();
    }

    @Override
    public String readAllHexString() throws IOException {
        return super.readAllHexString();
    }

    @Override
    public int readMultipleInt(int size) throws IOException {
        return super.readMultipleInt(size);
    }

    @Override
    public String[] readMultipleHexArray(int size) throws IOException {
        return super.readMultipleHexArray(size);
    }

    @Override
    public String readMultipleHexString(int size) throws IOException {
        return super.readMultipleHexString(size);
    }

    @Override
    public String readHexSting() throws IOException {
        return super.readHexSting();
    }

    public int read16Hex() throws IOException {
        int readHex = readHex();
        return readHex & 0xff;
    }

    @Override
    public int readHex() throws IOException {
        return super.readHex();
    }

    @Override
    public List<JHexByteArrayInputStream> unpacking(Function<Byte, Boolean> isHead, Function<Byte, Boolean> isTail) throws IOException {
        return super.unpacking(isHead, isTail);
    }

    @Override
    public List<JHexByteArrayInputStream> unpacking(Function<Byte, Boolean> isHead, Function<Byte, Boolean> isTail, boolean containFlag) throws IOException {
        return super.unpacking(isHead, isTail, containFlag);
    }

    //多字节拆包
    public List<JHexByteArrayInputStream> unpacks(Function<byte[], Boolean> isHead,
                                                  Function<byte[], Boolean> isTail,
                                                  boolean cf,
                                                  int headSize, int tailSize) throws IOException {
        boolean write = false;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<JHexByteArrayInputStream> result = new ArrayList<>();
        byte[] headBuf = new byte[headSize];
        int hi = 0;
        byte[] tailBuf = new byte[tailSize];
        int ti = 0;
        for (;;){
            int read = read();
            if (read == -1)
                break;

            if (!write){
                if (hi == headBuf.length){
                    out.write(headBuf[0]);
                    for (int i = 0; i < hi; i++) {
                        if (i != hi - 1)
                            headBuf[i] = headBuf[i + 1];
                    }
                    headBuf[hi - 1] = (byte) read;
                }else
                headBuf[hi ++] = (byte) read;
                if (hi == headBuf.length){
                    Boolean head;
                    try {

                        head = isHead.apply(headBuf);
                    } catch (Throwable e) {
                        throw new IOException(e);
                    }
                    Assert.notNull(head, "function result is must not null");
                    write = head;
                    if (cf && write){
                        out.write(headBuf);
                    }
                }

            }else {
                if (ti == tailBuf.length){
                    out.write(tailBuf[0]);
                    for (int i = 0; i < ti; i++) {
                        if (i != ti - 1)
                            tailBuf[i] = tailBuf[i + 1];
                    }
                    tailBuf[ti - 1] = (byte) read;
                }else
                    tailBuf[ti ++] = (byte) read;
                if (ti == tailBuf.length){
                    Boolean tail;
                    try {

                        tail = isTail.apply(tailBuf);
                    } catch (Throwable e) {
                        throw new IOException(e);
                    }
                    Assert.notNull(tail, "function result is must not null");
                    write = !tail;
                    if (tail){
                        if (cf)
                            out.write(tailBuf);
                        JHexByteArrayInputStream in = new JHexByteArrayInputStream(out.toByteArray());
                        result.add(in);
                        out.reset();
                        hi = 0;
                        ti = 0;
                    }
                }
            }
        }
        return result;
    }

    public BinaryPartElement readBinary() throws IOException {
        Object javaObject = readHexJavaObject();
        if (javaObject instanceof BinaryPartElement){
            return (BinaryPartElement) javaObject;
        }else {
            throw new IOException("is not a binary part");
        }
    }

    public BinaryPartElement readBinaryBytes() throws IOException {
        int type = readInt();
        BinaryPartElement element = BinaryUtils.instanceBinary(type);
        String name = readUTF();
        BinaryUtils.setValueInBinary(element, "name", name);
        int size = readInt();
        byte[] bytes = new byte[size];
        read(bytes);
        BinaryUtils.setValueInBinary(element, "buf", bytes);
        if (element instanceof BinaryFile){
            String fileName = readUTF();
            BinaryUtils.setValueInBinary(element, "fileName", fileName);
        }
        return element;
    }
}

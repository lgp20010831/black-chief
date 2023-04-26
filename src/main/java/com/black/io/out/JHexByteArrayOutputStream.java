package com.black.io.out;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.util.IoUtil;
import com.black.utils.JHex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class JHexByteArrayOutputStream extends DataByteBufferArrayOutputStream{

    public JHexByteArrayOutputStream() {
    }

    public JHexByteArrayOutputStream(OutputStream writeOut) {
        super(writeOut);
    }

    @Override
    public JHexByteArrayInputStream getInputStream() {
        return new JHexByteArrayInputStream(toByteArray());
    }

    @Override
    public void writeHexString(String hexString) throws IOException {
        if (hexString == null || hexString.length() == 0)
            return;
        super.writeHexString(hexString);
    }

    @Override
    public void writeHex(int hex) throws IOException {
        super.writeHex(hex);
    }

    @Override
    public void writeFullHex(int hex) throws IOException {
        super.writeFullHex(hex);
    }

    @Override
    public void writeHex(int hex, int size) throws IOException {
        super.writeHex(hex, size);
    }

    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        super.write(b);
    }

    public void writeChineseHex(String chinese) throws IOException {
        writeHexString(JHex.castChineseToHex(chinese));
    }

    public void writeHexObject(Object source) throws IOException {
        String str16 = JHex.encodeObject(source);
        writeHexString(str16);
    }

    public void writeHexJavaObject(Object object) throws IOException {
        byte[] bytes = IoUtil.toBuffer(object);
        int length = bytes.length;
        String str16 = JHex.encodeString(bytes);
        writeInt(length);
        writeHexString(str16);
    }

    public void writeBinary(BinaryPartElement binaryPartElement) throws IOException {
        writeHexJavaObject(binaryPartElement);
    }

    public void writeBinaryByte(BinaryPartElement binaryPartElement) throws IOException {
        int type = BinaryUtils.typeOfBinary(binaryPartElement);
        writeInt(type);
        writeUTF(binaryPartElement.getName());
        writeInt(binaryPartElement.size());
        write(binaryPartElement.buf());
        if (binaryPartElement instanceof BinaryFile){
            writeUTF(((BinaryFile) binaryPartElement).getFileName());
        }
    }
}

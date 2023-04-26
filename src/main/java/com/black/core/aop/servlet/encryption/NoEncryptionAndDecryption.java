package com.black.core.aop.servlet.encryption;

public class NoEncryptionAndDecryption implements Encryption, Decryption{
    @Override
    public byte[] decode(String ciphertext) {
        if (ciphertext == null){
            return new byte[0];
        }
        return ciphertext.getBytes();
    }

    @Override
    public String encode(byte[] byteArray) {
        return new String(byteArray);
    }
}

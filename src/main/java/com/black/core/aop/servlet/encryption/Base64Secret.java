package com.black.core.aop.servlet.encryption;

import com.black.core.util.Base64Utils;

public class Base64Secret implements Decryption, Encryption{
    @Override
    public byte[] decode(String ciphertext) {
        if (ciphertext == null){
            return new byte[0];
        }
        return Base64Utils.primordialDecode(ciphertext);
    }

    @Override
    public String encode(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        return Base64Utils.primordialEecode(byteArray);
    }
}

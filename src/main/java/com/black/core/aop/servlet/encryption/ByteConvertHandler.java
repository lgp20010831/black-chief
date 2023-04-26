package com.black.core.aop.servlet.encryption;

import com.black.core.convert.ConversionWay;
import com.black.core.convert.TypeContributor;

@TypeContributor
public class ByteConvertHandler {


    @ConversionWay
    byte[] convertByte(String str){
        if (str == null){
            return new byte[0];
        }
        return str.getBytes();
    }

    @ConversionWay
    byte[] objectConvertByte(Object obj){
        if (obj == null){
            return new byte[0];
        }
        return convertByte(obj.toString());
    }
}

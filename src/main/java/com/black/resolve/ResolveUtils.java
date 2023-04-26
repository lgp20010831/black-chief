package com.black.resolve;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.utils.IoUtils;

public class ResolveUtils {


    public static JHexByteArrayInputStream wrapperInputStream(Object source){
        byte[] bytes = IoUtils.getBytes(source, false);
        return new JHexByteArrayInputStream(bytes);
    }


}

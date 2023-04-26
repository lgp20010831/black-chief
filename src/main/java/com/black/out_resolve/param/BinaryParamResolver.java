package com.black.out_resolve.param;

import com.black.io.out.BinaryFile;
import com.black.io.out.BinaryPartElement;
import com.black.io.out.BinaryString;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.resolve.annotation.BinaryPart;
import com.black.core.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;

public class BinaryParamResolver extends AbstractParamResolver{
    @Override
    public boolean support(Parameter parameter) {
        return parameter.isAnnotationPresent(BinaryPart.class);
    }

    @Override
    public void resolve(JHexByteArrayOutputStream outputStream, Parameter parameter, Object value) throws IOException {
        BinaryPart annotation = parameter.getAnnotation(BinaryPart.class);
        Class<?> type = parameter.getType();
        String name = StringUtils.hasText(annotation.value()) ? annotation.value() : parameter.getName();
        BinaryPartElement partElement = null;
        if (value == null){
            partElement = new BinaryString(name, new byte[0]);
        }else if (type.equals(File.class)){
            File file = (File) value;
            partElement = new BinaryFile(name, file);
        }else if (MultipartFile.class.isAssignableFrom(type)){
            MultipartFile multipartFile = (MultipartFile) value;
            partElement = new BinaryFile(name, multipartFile.getOriginalFilename(), multipartFile.getBytes());
        }else if (String.class.equals(type)){
            String str = value.toString();
            partElement = new BinaryString(name, str);
        }else {
            log.error("not support binary type: {}", type);
        }
        if(partElement != null){
            outputStream.writeBinaryByte(partElement);
        }
    }
}

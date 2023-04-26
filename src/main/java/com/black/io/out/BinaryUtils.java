package com.black.io.out;

import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.BeanUtil;

public class BinaryUtils {

    public static BinaryPartElement instanceBinary(int type){
        switch (type){
            case 0:
                return new BinaryString();
            case 1:
                return new BinaryFile();
            default:
                throw new IllegalStateException("not type of binary: " + type);
        }
    }

    public static int typeOfBinary(BinaryPartElement element){
        if (element instanceof BinaryString){
            return 0;
        }else if (element instanceof BinaryFile){
            return 1;
        }else {
            return -1;
        }
    }

    public static void setValueInBinary(BinaryPartElement element, String name, Object value){
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(element);
        FieldWrapper field = wrapper.getField(name);
        if (field != null){
            field.setValue(element, value);
        }
    }



}

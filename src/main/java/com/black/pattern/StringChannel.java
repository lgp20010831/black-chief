package com.black.pattern;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

public class StringChannel extends AbstractChannel<String, String>{

    Field field;

    public StringChannel(){
        this("");
    }

    public StringChannel(String target) {
        super(target);

        try {
            field = getTarget().getClass().getDeclaredField("value");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static StringChannel open(String target){
        return new StringChannel(target);
    }

    @Override
    public void write(String s) {
        setValue(s);
    }


    private void setValue(String s){
        try {
            field.set(getTarget(), (getTarget() + s).toCharArray());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(getTarget().getBytes());
    }

}

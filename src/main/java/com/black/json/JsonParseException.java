package com.black.json;

public class JsonParseException extends RuntimeException{
    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, String text, int index) {
        super(wrapperMessage(message, text, index));
    }

    public JsonParseException(String message, Throwable cause, String text, int index) {
        super(wrapperMessage(message, text, index), cause);
    }

    public static String wrapperMessage(String message, String text, int index){
        return message + "\n index: " + index + " tip: " + Util.getErrorTextTip(text, index);
    }
}

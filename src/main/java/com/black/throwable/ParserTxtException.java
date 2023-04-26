package com.black.throwable;

/** 当解析文本发生异常时, 通常抛出该异常 */
public class ParserTxtException extends RuntimeException{


    public ParserTxtException() {
    }

    public ParserTxtException(String message) {
        super(message);
    }

    public ParserTxtException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParserTxtException(Throwable cause) {
        super(cause);
    }
}

package com.black.core.aop.servlet.item;

public abstract class AbstractLAOperatorProcessor implements LAOperatorProcessor{

    protected char startChar, endChar;

    public AbstractLAOperatorProcessor(char startChar){
        this(startChar, EMTRY_CHAR);
    }

    public AbstractLAOperatorProcessor(char startChar, char endChar) {
        this.startChar = startChar;
        this.endChar = endChar;
    }

    @Override
    public char getEndChar() {
        return endChar;
    }

    @Override
    public char getStartChar() {
        return startChar;
    }
}

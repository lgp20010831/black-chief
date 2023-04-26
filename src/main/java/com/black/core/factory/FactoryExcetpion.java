package com.black.core.factory;

public class FactoryExcetpion extends RuntimeException {


    public FactoryExcetpion(String message) {
        super(message);
    }

    public FactoryExcetpion(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryExcetpion(Throwable cause) {
        super(cause);
    }
}

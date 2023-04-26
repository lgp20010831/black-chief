package com.black.vfs;

public class VfsLoadException extends RuntimeException{


    public VfsLoadException() {
    }

    public VfsLoadException(String message) {
        super(message);
    }

    public VfsLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public VfsLoadException(Throwable cause) {
        super(cause);
    }
}

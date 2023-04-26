package com.black.graphql;

public class GraphqlTransferException extends RuntimeException{


    public GraphqlTransferException() {
    }

    public GraphqlTransferException(String message) {
        super(message);
    }

    public GraphqlTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphqlTransferException(Throwable cause) {
        super(cause);
    }
}

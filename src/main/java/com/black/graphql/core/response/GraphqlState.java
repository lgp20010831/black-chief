package com.black.graphql.core.response;

@SuppressWarnings("all")
public enum GraphqlState {
    SUCCESS(0),
    FAILED(-1);

    private int value;

    private GraphqlState(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }

    public static GraphqlState getEnum(int i) {
        switch (i) {
            case 1:
                return FAILED;
            default:
                return SUCCESS;
        }
    }
}

package com.black.core.spring.event;

public class ApplicationEvent {

    private final int state;

    public final static ApplicationEvent create = new ApplicationEvent(1);

    public final static ApplicationEvent pattern = new ApplicationEvent(2);

    public final static ApplicationEvent load = new ApplicationEvent(3);

    public final static ApplicationEvent dormancy = new ApplicationEvent(4);

    public static final ApplicationEvent distory = new ApplicationEvent(5);

    public ApplicationEvent(int state) {
        this.state = state;
    }
}

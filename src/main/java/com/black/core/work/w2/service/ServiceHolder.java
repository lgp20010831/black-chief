package com.black.core.work.w2.service;

public class ServiceHolder {

    private static WorkflowService service;

    public static void setService(WorkflowService service) {
        ServiceHolder.service = service;
    }

    public static WorkflowService getService() {
        return service;
    }
}

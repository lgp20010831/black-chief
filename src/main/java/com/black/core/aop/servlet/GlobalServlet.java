package com.black.core.aop.servlet;

public class GlobalServlet {

    static boolean eyeCatchingLog = true;

    public static boolean isEyeCatchingLog() {
        return eyeCatchingLog;
    }

    public static void openEyeLog(){
        eyeCatchingLog = true;
    }

    public static void closeEyeLog(){
        eyeCatchingLog = false;
    }
}

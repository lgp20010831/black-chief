package com.black.core.log;

public enum LogLevel {


    INFO("INFO"), DEBUG("DEBUG"), TRACE("TRACE"), ERROR("ERROR"), THROWABLE("THROWABLED");

    final String level;

    LogLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}

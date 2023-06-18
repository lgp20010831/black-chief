package com.black.arg.filter;

public interface DepthAnalysisFieldFilter {


    boolean canAnalysis(String name, Class<?> type, Object bean);
}

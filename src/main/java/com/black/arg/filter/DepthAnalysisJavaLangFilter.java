package com.black.arg.filter;

public class DepthAnalysisJavaLangFilter implements DepthAnalysisFieldFilter{


    @Override
    public boolean canAnalysis(String name, Class<?> type, Object bean) {
        return !type.getName().startsWith("java.");
    }
}

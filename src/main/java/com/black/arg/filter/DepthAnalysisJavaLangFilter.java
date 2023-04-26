package com.black.arg.filter;

import com.black.core.query.FieldWrapper;

public class DepthAnalysisJavaLangFilter implements DepthAnalysisFieldFilter{


    @Override
    public boolean canAnalysis(FieldWrapper fw, Object bean) {
        Class<?> type = fw.getType();
        return !type.getName().startsWith("java.");
    }
}

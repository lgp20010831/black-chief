package com.black.arg.filter;

import com.black.core.query.FieldWrapper;

public interface DepthAnalysisFieldFilter {


    boolean canAnalysis(FieldWrapper fw, Object bean);
}

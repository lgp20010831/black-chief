package com.black.core.sql.code.pattern;

import com.black.pattern.PipeNode;
import com.black.pattern.Pipeline;

public interface PipelineHook<P extends PipeNode<A, B>, A, B> {


    void callback(Pipeline<P, A, B> pipeline);

}

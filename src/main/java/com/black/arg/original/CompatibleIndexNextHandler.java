package com.black.arg.original;

public class CompatibleIndexNextHandler extends AbstractCompatibleIndexHandler{
    @Override
    public boolean support(OriginalArgStrategy strategy) {
        return strategy == OriginalArgStrategy.compatible_index_next;
    }

}

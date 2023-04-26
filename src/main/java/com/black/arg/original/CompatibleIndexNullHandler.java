package com.black.arg.original;

public class CompatibleIndexNullHandler extends AbstractCompatibleIndexHandler{
    @Override
    public boolean support(OriginalArgStrategy strategy) {
        return strategy == OriginalArgStrategy.compatible_index_null;
    }

    @Override
    public boolean canNext(Object arg) {
        return false;
    }
}

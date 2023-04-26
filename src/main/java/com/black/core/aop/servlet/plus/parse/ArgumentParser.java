package com.black.core.aop.servlet.plus.parse;

import com.black.core.aop.servlet.plus.EntryWrapper;
import com.black.core.aop.servlet.plus.PlusMethodWrapper;

public interface ArgumentParser {


    boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper);


    Object[] parseArgument(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper, Object[] args);
}

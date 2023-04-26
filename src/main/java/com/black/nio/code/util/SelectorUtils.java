package com.black.nio.code.util;

import com.black.nio.code.SelectorWrapper;

import java.io.IOException;
import java.nio.channels.Selector;


public class SelectorUtils {

    public static Selector openSelector() throws IOException {
        return new SelectorWrapper();
    }
}

package com.black.scan;

import com.black.core.util.IntegratorScanner;
import com.black.core.util.SimplePattern;

import java.util.Set;

public class ProxySimplePatternScanner extends AbstractChiefScanner{

    private final SimplePattern simplePattern;

    public ProxySimplePatternScanner() {
        simplePattern = new IntegratorScanner();
    }


    @Override
    public Set<Class<?>> doLoad(String classpath) {
        return simplePattern.scan(classpath);
    }
}

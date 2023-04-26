package com.black.scan;

import com.black.utils.FineScanner;

import java.util.Set;

public class ChiefFineScanner extends AbstractChiefScanner{


    private final FineScanner fineScanner;

    public ChiefFineScanner() {
        fineScanner = new FineScanner();
    }

    @Override
    public Set<Class<?>> load(String classpath) {
        return fineScanner.scan(classpath, getClassLoader());
    }
}

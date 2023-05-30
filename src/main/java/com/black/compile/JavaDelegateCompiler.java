package com.black.compile;

@SuppressWarnings("all")
public class JavaDelegateCompiler extends AbstractDelegateCompiler{

    public JavaDelegateCompiler(){
        importDependencyPackage("java.lang", "java.util", "java.net", "java.io", "java.math",
                "java.sql", "java.time");
    }

}

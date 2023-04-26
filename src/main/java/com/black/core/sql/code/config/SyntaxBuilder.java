package com.black.core.sql.code.config;

public class SyntaxBuilder {

    public static SyntaxConfigurer blend(String syntax){
        return new SyntaxConfigurer().setBlendSyntax(syntax);
    }

    public static SyntaxConfigurer set(String... syntaxs){
        return new SyntaxConfigurer().addSets(syntaxs);
    }

    public static SyntaxConfigurer sequences(String... syntaxs){
        return new SyntaxConfigurer().addSequences(syntaxs);
    }

    public static SyntaxConfigurer dict(String... syntaxs){
        return new SyntaxConfigurer().addDicts(syntaxs);
    }

    public static SyntaxConfigurer apply(String sql){return new SyntaxConfigurer().applySql(sql);}
}

package com.black.core.sql.conformity;

public class ConformityResultManager {

    private static ThreadLocal<StatementConformity> local = new ThreadLocal<>();

    public static StatementConformity currentConformity(){
        return local.get();
    }

    public static StatementConformity currentConformityAndInit(){
        StatementConformity conformity = local.get();
        if (conformity == null){

        }
        return conformity;
    }

    public static void setCurrentConformity(StatementConformity currentConformity){
        local.set(currentConformity);
    }
}

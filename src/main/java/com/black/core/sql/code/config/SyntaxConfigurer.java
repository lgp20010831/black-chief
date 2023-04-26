package com.black.core.sql.code.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuppressWarnings("all")
public class SyntaxConfigurer {

    private String blendSyntax;

    private List<String> dictSyntaxs = new ArrayList<>();

    private List<String> sequencesSyntaxs = new ArrayList<>();

    private List<String> setSyntaxs = new ArrayList<>();

    private List<String> returnColumns = new ArrayList<>();

    private StatementValueSetDisplayConfiguration statementValueSetDisplayConfiguration;

    public static class StatementSetConfigurationBuilder{
        private final StatementValueSetDisplayConfiguration displayConfiguration;

        private final SyntaxConfigurer configurer;

        public StatementSetConfigurationBuilder(SyntaxConfigurer configurer) {
            this.configurer = configurer;
            displayConfiguration = new StatementValueSetDisplayConfiguration();
        }

        public StatementSetConfigurationBuilder setObjectOfType(boolean setObjectOfType){
            displayConfiguration.setSetObjectOfType(setObjectOfType);
            return this;
        }

        public SyntaxConfigurer end(){
            return configurer.setStatementValueSetDisplayConfiguration(displayConfiguration);
        }
    }

    private String applySql;

    public SyntaxConfigurer setStatementValueSetDisplayConfiguration(StatementValueSetDisplayConfiguration statementValueSetDisplayConfiguration) {
        this.statementValueSetDisplayConfiguration = statementValueSetDisplayConfiguration;
        return this;
    }

    public StatementSetConfigurationBuilder setObject(){
        return new StatementSetConfigurationBuilder(this);
    }

    public SyntaxConfigurer addResultColumns(String... syntaxs){
        for (String syntax : syntaxs) {
            returnColumns.add(syntax);
        }
        return this;
    }

    public SyntaxConfigurer applySql(String sql){
        applySql = sql;
        return this;
    }

    public SyntaxConfigurer addDicts(String... syntaxs){
        for (String syntax : syntaxs) {
            dictSyntaxs.add(syntax);
        }
        return this;
    }

    public SyntaxConfigurer addSequences(String... syntaxs){
        for (String syntax : syntaxs) {
            sequencesSyntaxs.add(syntax);
        }
        return this;
    }

    public SyntaxConfigurer addSets(String... syntaxs){
        for (String syntax : syntaxs) {
            setSyntaxs.add(syntax);
        }
        return this;
    }

    public SyntaxConfigurer setBlendSyntax(String blendSyntax) {
        this.blendSyntax = blendSyntax;
        return this;
    }

}

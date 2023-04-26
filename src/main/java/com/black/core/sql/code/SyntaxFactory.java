package com.black.core.sql.code;

import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntaxFactory {


    public static SyntaxExecutor create(GlobalParentMapping mapping){
        return new SyntaxExecutor(mapping);
    }

    @Getter
    public static class SyntaxExecutor{
        GlobalParentMapping mapping;

        SyntaxConfigurer configurer;

        String name;

        Map<String, Object> condition = new HashMap<>();

        int validReferences = 1;

        public SyntaxExecutor(){}

        public SyntaxExecutor(GlobalParentMapping mapping) {
            this.mapping = mapping;
            configurer = new SyntaxConfigurer();
        }

        public SyntaxExecutor mapper(GlobalParentMapping parentMapping){
            mapping = parentMapping;
            return this;
        }

        public SyntaxExecutor apply(String applySql){
            configurer.applySql(applySql);
            return this;
        }

        public SyntaxExecutor setConfigurer(SyntaxConfigurer configurer) {
            this.configurer = configurer;
            return this;
        }

        public SyntaxExecutor dicts(String... syntaxs){
            configurer.addDicts(syntaxs);
            return this;
        }

        public SyntaxExecutor sequences(String... syntaxs){
            configurer.addSequences(syntaxs);
            return this;
        }

        public SyntaxExecutor sets(String... syntaxs){
            configurer.addSets(syntaxs);
            return this;
        }

        public SyntaxExecutor resultColumns(String... syntaxs){
            configurer.addResultColumns(syntaxs);
            return this;
        }

        public SyntaxExecutor blend(String syntaxs){
            configurer.setBlendSyntax(syntaxs);
            return this;
        }

        public SyntaxExecutor name(String name){
            this.name = name;
            return this;
        }

        public SyntaxExecutor validReferences(int validReferences){
            this.validReferences = validReferences;
            return this;
        }

        public SyntaxExecutor condition(Map<String, Object> condition) {
            if (condition != null){
                this.condition.putAll(condition);
            }
            return this;
        }

        public SyntaxExecutor clear() {
            condition.clear();
            return this;
        }

        public List<Map<String, Object>> list(){
            Assert.notNull(name, "unset table name");
            mapping.configureSyntax(configurer, validReferences);
            return mapping.globalDictSelect(name, condition, null, null);
        }

        public Map<String, Object> single(){
            return SQLUtils.getSingle(list());
        }


    }


}

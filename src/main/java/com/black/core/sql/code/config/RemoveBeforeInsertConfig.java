package com.black.core.sql.code.config;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemoveBeforeInsertConfig extends ConfigurerAdapter{

    String blendVoice;

    public RemoveBeforeInsertConfig(Configuration configuration) {
        super(configuration);
    }
}

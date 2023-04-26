package com.black.core.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ItemParams {

    private final String item;
    private final Object[] args;

}

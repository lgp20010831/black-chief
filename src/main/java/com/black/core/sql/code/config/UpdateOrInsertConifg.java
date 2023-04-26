package com.black.core.sql.code.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class UpdateOrInsertConifg {

    String[] sqlSeqs;

    Set<String> addingExist;

    String[] existCondition;


}

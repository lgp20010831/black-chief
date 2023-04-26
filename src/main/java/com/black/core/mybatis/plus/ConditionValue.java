package com.black.core.mybatis.plus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor  @Getter
public class ConditionValue {

    Object entryValue;

    boolean and;

    boolean like;
}

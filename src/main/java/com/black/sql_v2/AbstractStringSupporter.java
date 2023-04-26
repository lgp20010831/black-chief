package com.black.sql_v2;

import com.black.core.util.StringUtils;

public abstract class AbstractStringSupporter implements ObjectParamSupporter{

    protected final String prefix;

    public AbstractStringSupporter(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    protected String getTxt(Object context){
        String value = String.valueOf(context);
        String ignoreCase = StringUtils.removeIfStartWithIgnoreCase(value, prefix);
        return StringUtils.removeFrontSpace(ignoreCase);
    }

    @Override
    public boolean support(Object param) {
        return param instanceof String && StringUtils.startsWithIgnoreCase(String.valueOf(param), prefix);
    }
}

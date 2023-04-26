package com.black.syntax;

import com.black.core.util.StringUtils;

public abstract class AbstractSyntaxResolver implements SyntaxExpressionResolver{

    String flag;


    public AbstractSyntaxResolver(String flag) {
        this.flag = flag;
    }

    public AbstractSyntaxResolver() {
    }

    public String getFlag() {
        return flag;
    }

    @Override
    public boolean supportType(String item) {
        String flag = getFlag();
        return flag != null && item != null && item.startsWith(flag);
    }

    @Override
    public String cutItem(String item) {
        String flag = getFlag();
        return flag == null ? item : StringUtils.removeIfStartWith(item, flag);
    }


}

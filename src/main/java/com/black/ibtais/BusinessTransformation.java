package com.black.ibtais;

import com.black.core.sql.code.AliasColumnConvertHandler;

public interface BusinessTransformation {

    AliasColumnConvertHandler getConvertHandler();

    String getBlendString();
}

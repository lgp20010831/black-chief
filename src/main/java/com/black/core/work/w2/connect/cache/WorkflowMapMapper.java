package com.black.core.work.w2.connect.cache;

import com.black.core.sql.DefaultNoParseAliasAndColumnHandler;
import com.black.core.sql.annotation.ExcludeScanMapper;
import com.black.core.sql.annotation.GlobalConfiguration;
import com.black.core.sql.code.mapping.GlobalParentMapping;

@ExcludeScanMapper
@GlobalConfiguration(value = "workflow", convertHandlerType = DefaultNoParseAliasAndColumnHandler.class)
public interface WorkflowMapMapper extends GlobalParentMapping {



}

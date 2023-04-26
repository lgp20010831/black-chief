package com.black.core.sql.action;

import com.black.core.sql.code.config.SyntaxBuilder;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.config.SyntaxRangeConfigurer;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

import static com.black.utils.ServiceUtils.*;

@Log4j2
public abstract class DictDynamicController<M extends GlobalParentMapping> extends AutoMapperController<M>{

    public static String[] suffix = new String[]{"Controller", "Servlet", "Action", "Handler", "Processor"};

    private String tableName;

    @Override
    public String getTableName() {
        if(tableName == null){
            String name = name();
            String className = StringUtils.removeIfEndWiths(name, suffix);
            tableName = StringUtils.unruacnl(className);
            if (log.isDebugEnabled()){
                log.debug("[{}] of table name: [{}]", name, tableName);
            }
        }
        return tableName;
    }

    protected String getSelectBlendString(){
        return null;
    }

    protected String[] getSelectDictString(){
        return null;
    }

    protected String getSelectListApplySql(){
        return null;
    }

    @Override
    protected Object doSelectById(Object id) {
        Map<String, Object> param = ofMap(getPrimaryKey(), id);
        return getMapper().globalDictSelectSingle(getTableName(), param, getSelectBlendString(), getSelectDictString());
    }

    @Override
    protected Object doSelectList(Map<String, Object> body) {
        String selectListApplySql = getSelectListApplySql();
        if (StringUtils.hasText(selectListApplySql)){
            final M mapper = getMapper();
            SyntaxRangeConfigurer rangeConfigurer = SyntaxManager.getRangeConfigurer(mapper.getAlias());
            if (rangeConfigurer == null){
                SyntaxConfigurer configurer = SyntaxBuilder.apply(selectListApplySql);
                mapper.configureSyntax(configurer, 1);
            }else {
                SyntaxConfigurer configurer = rangeConfigurer.getConfigurer();
                configurer.applySql(selectListApplySql);
            }
        }
        return getMapper().globalDictSelect(getTableName(), body, getSelectBlendString(), getSelectDictString());
    }


}

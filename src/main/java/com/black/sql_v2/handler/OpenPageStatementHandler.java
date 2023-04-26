package com.black.sql_v2.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.api.FormatParser;
import com.black.page.PageCache;
import com.black.core.log.IoLog;
import com.black.core.sql.code.page.Page;
import com.black.core.sql.code.page.PageHelper;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.GlobalEnvironment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.utils.SqlV2Utils;

public class OpenPageStatementHandler extends AbstractStringSupporter implements SqlStatementHandler{

    public static final String PREFIX = "open page";

    private final FormatParser formatParser;

    public static final String PS_NAME = "pageSize";

    public static final String PN_NAME = "pageNum";

    public OpenPageStatementHandler() {
        super(PREFIX);
        formatParser = new FormatParser();
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        GlobalEnvironment instance = GlobalEnvironment.getInstance();
        IoLog log = instance.getLog();
        String txt = getTxt(param);
        Integer pageSize = null, pageNum = null;
        if (StringUtils.hasText(txt)){
            try {
                JSONObject json = JSONObject.parseObject(formatParser.parseJson(txt));
                pageSize = json.getInteger(PS_NAME);
                pageNum = json.getInteger(PN_NAME);
            }catch (Throwable e){
                log.error("[SQL] -- parse [{}] page info fail: {}", txt, e.getMessage());
            }
        }

        if (pageSize == null || pageNum == null){
            SqlV2Utils.PageInfoWrapper infoByMap = SqlV2Utils.getPageInfoByMap(JDBCEnvironmentLocal.getEnv(), PS_NAME, PN_NAME);
            if (infoByMap == null){
                log.error("[SQL] -- can not find page info in env, will do not open page");
                return statement;
            }
            pageSize = infoByMap.getPageSize();;
            pageNum = infoByMap.getPageNum();
        }
        Page<?> page = PageHelper.openPage(pageNum, pageSize);
        PageCache.setPage(page);
        return statement;
    }

}

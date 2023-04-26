package com.black.core.sql.code.listener;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.LockTables;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.table.TableUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2 @SQLListener
public class LockTablesListener implements GlobalSQLRunningListener {

    @Override
    public void beforeInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Object[] args) {
        if (mw.hasAnnotation(LockTables.class)) {
            LockTables lockTables;
            String[] tables = (lockTables = mw.getAnnotation(LockTables.class)).value();
            TableUtils.locks(lockTables.type(), globalSQLConfiguration.getConnection(), tables);
        }
    }
}

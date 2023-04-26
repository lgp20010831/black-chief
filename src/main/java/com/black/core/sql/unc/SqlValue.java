package com.black.core.sql.unc;

import com.black.core.sql.code.SetPrepareStatement;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.config.StatementSetConfigurationLocal;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.core.sql.code.util.SQLUtils;
import com.black.table.ColumnMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;

@Getter @Setter @AllArgsConstructor
public class SqlValue implements SetPrepareStatement {

    SqlVariable variable;

    Object value;

    ColumnMetadata columnMetadata;

    @Override
    public void setValue(StatementWrapper statement) throws SQLException {
        StatementValueSetDisplayConfiguration configuration = StatementSetConfigurationLocal.getSetValueConfiguration();
        int type = columnMetadata.getType();
        int index = variable.getIndex();
        SQLUtils.setStatementValue(statement.getPreparedStatement(), index, value, type, configuration);
    }
}

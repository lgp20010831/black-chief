package com.black.sql_v2.utils;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.Assert;
import com.black.table.ForeignKey;
import com.black.table.TableMetadata;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SubsetInfo {

    private final TableMetadata masterTable;

    private final TableMetadata subTable;

    private final String masterTableName;

    private String masterIdName;

    private String masterIdAlias;

    private final String subTableName;

    private String subIdName;

    private String subTableAlias;

    private String subIdAlias;

    private String applySql = "";

    private String suffix = "List";

    private String createBy;

    private String watchword;

    private boolean oneMany = true;

    public SubsetInfo(TableMetadata masterTable, TableMetadata subTable) {
        this.masterTable = masterTable;
        this.subTable = subTable;
        masterIdName = masterTable.firstPrimaryKey().getName();
        masterTableName = masterTable.getTableName();
        subTableName = subTable.getTableName();
        subTableAlias = subTableName;
    }

    public void setMasterIdName(String masterIdName) {
        this.masterIdName = masterIdName;
    }

    public void setSubIdName(String subIdName) {
        this.subIdName = subIdName;
        subIdAlias = subIdName;
    }

    public void foreignKeyLookup(){
        ForeignKey foreign = subTable.getForeignByPrimaryNameAndTableName(masterIdName, masterTableName);
        Assert.notNull(foreign, "can not find foreign mapping for " + masterIdName);
        subIdName = foreign.getName();
        subIdAlias = subIdName;
    }

    public void convert(AliasColumnConvertHandler convertHandler){
        subIdAlias = convertHandler.convertAlias(subIdName);
        subTableAlias = convertHandler.convertAlias(subTableName);
        masterIdAlias = convertHandler.convertAlias(masterIdName);
    }

    public String getApplySql() {
        return applySql == null ? "" : applySql;
    }

    public String getSuffix() {
        return suffix == null ? "List" : suffix;
    }
}

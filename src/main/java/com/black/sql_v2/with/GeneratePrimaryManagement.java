package com.black.sql_v2.with;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.utils.ServiceUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

public class GeneratePrimaryManagement {

    private static final ThreadLocal<WaitGenerateWrapper> waitFillManager = new ThreadLocal<>();

    public static void close(String passId){
        WaitGenerateWrapper wrapper = waitFillManager.get();
        if (wrapper != null && passId.equals(wrapper.getPassId())){
            waitFillManager.remove();
        }
    }

    public static WaitGenerateWrapper get(){
        return waitFillManager.get();
    }

    public static void register(WaitGenerateWrapper bean){
        waitFillManager.set(bean);
    }

    public static List<Object> handler(ResultSet resultSet){
        WaitGenerateWrapper wrapper = waitFillManager.get();
        try {
            if (wrapper == null){
                return null;
            }
            AliasColumnConvertHandler convertHandler = wrapper.getConvertHandler();
            String tableName = wrapper.getTableName();
            TableMetadata tableMetadata = SqlV2Utils.tryGetMetadata(tableName);
            PrimaryKey primaryKey = tableMetadata.firstPrimaryKey();
            List<Object> list = SQLUtils.wrapList(wrapper.getBean());
            ResultSetMetaData metaData = resultSet.getMetaData();
            int index = -1;
            while (resultSet.next()) {
                ++index;
                Object bean;
                if (index <= list.size() - 1){
                    bean = list.get(index);
                }else {
                    break;
                }
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = primaryKey == null ? metaData.getColumnName(i) : primaryKey.getName();
                    //主键
                    Object key = resultSet.getObject(i);
                    //字段名
                    String alias = convertHandler.convertAlias(columnName);
                    ServiceUtils.setProperty(bean, alias, key);
                }
            }
            return list;
        }catch (Throwable e){
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeResultSet(resultSet);
        }
    }
}

package com.black.core.sql.code;

import com.black.core.util.Assert;
import com.black.utils.LocalMap;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class SaveManager {

    private final static LocalMap<String, Map<String, Savepoint>> savepointLocalMap = new LocalMap<>();

    /** key = 数据源别名, val = 保存点别名 */
    private final static LocalMap<String, String> savePointLocal = new LocalMap<>();

    public static void registerSavePoint(String datasourceAlias, String alias, Savepoint savepoint){
        if (TransactionSQLManagement.isActivity(datasourceAlias)) {
            Map<String, Savepoint> map = savepointLocalMap.computeIfAbsent(datasourceAlias, da -> new HashMap<>());
            map.put(alias, savepoint);
        }else {
            log.warn("当前数据源: [{}] 并不存活于事务管控中", datasourceAlias);
        }
    }

    public static Savepoint getSavePoint(String datasourceAlias, String alias){
        Map<String, Savepoint> map = savepointLocalMap.get(datasourceAlias);
        return map == null ? null : map.get(alias);
    }

    public static void clear(){
        savepointLocalMap.clear();
        savePointLocal.clear();
    }

    public static String getSavePointAlias(String das){
        return savePointLocal.get(das);
    }

    public static void pointSave(@NonNull String dsa, @NonNull String alias){
        String pre = savePointLocal.get(dsa);
        if (pre != null){
            log.info("替换事务保存点, 新事物保存点: [{}], 抛弃的事务保存点: [{}]", alias, pre);
        }
        Savepoint savePoint = getSavePoint(dsa, alias);
        Assert.notNull(savePoint, "不存在指定的事务保存点: " + alias);
        savePointLocal.put(dsa, alias);
    }
}

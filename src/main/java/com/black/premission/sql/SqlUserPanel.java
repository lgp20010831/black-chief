package com.black.premission.sql;

import com.black.GlobalVariablePool;
import com.black.premission.collect.SqlCollector;
import com.black.sql_v2.Sql;
import com.black.user.User;
import com.black.user.UserPanel;
import com.black.utils.ServiceUtils;

@SuppressWarnings("all")
public interface SqlUserPanel<P extends User> extends UserPanel<P>, SqlPanel<P> {

    @Override
    default String getTableName() {
        return GlobalVariablePool.RUP_MS_USER_TABLE_NAME;
    }

    @Override
    default Class entityType(){
        return SqlCollector.RUPEntityManager.getInstance().getUserEntity();
    }

    @Override
    default P getUser(String account){
        return (P) Sql.query(getTableName(), ServiceUtils.ofMap(getAccountName(), account)).javaSingle(entityType());
    }

    @Override
    default boolean updateUser(String oldAccount, P newUser){
        Sql.update(getTableName(), newUser, ServiceUtils.ofMap(getAccountName(), oldAccount));
        return true;
    }

    @Override
    default boolean joinUser(P user){
        join(user);
        return true;
    }

    @Override
    default boolean deleteUser(String a){
        Sql.delete(getTableName(), ServiceUtils.ofMap(getAccountName(), a));
        return true;
    }
}

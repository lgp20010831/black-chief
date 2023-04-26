package com.black.user;

import com.black.premission.Panel;
import com.black.core.tools.BeanUtil;

import static com.black.GlobalVariablePool.RUP_USER_ACCOUNT_NAME;

public interface UserPanel<U extends User> extends Panel<U> {

    default String getAccountName(){
        Class<? extends UserPanel> primordialClass = BeanUtil.getPrimordialClass(this.getClass());
        Account annotation = primordialClass.getAnnotation(Account.class);
        return annotation == null ? RUP_USER_ACCOUNT_NAME : annotation.value();
    }

    U getUser(String account);

    boolean updateUser(String oldAccount, U newUser);

    boolean joinUser(U user);

    boolean deleteUser(String a);
}

package com.black.user;

import com.black.core.spring.ApplicationHolder;
import com.black.core.util.StringUtils;
import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;

public class UserControl<U extends User> {

    private final UserPanel<U> panel;

    private final Cache<String, U> cache;

    public UserControl(Class<? extends UserPanel<U>> type){
        BeanFactory factory = ApplicationHolder.getBeanFactory();
        if (factory == null){
            throw new UsersException("无法实例化 panel");
        }
        panel = factory.getBean(type);
        cache = new Cache<>();
    }

    public UserControl(@NonNull UserPanel<U> panel) {
        this.panel = panel;
        cache = new Cache<>();
    }

    //加密, 子类重写
    protected String encrypt(String pwd){
        return pwd;
    }

    //解密
    protected String decrypt(String pwd){
        return pwd;
    }

    protected boolean check(String pwd, User databaseUser){
        return encrypt(pwd).equals(databaseUser.getPassword());
    }

    public boolean login(U u) throws UserNotExitsException {
        return login(u.getAccount(), u.getPassword());
    }

    //登录
    public boolean login(String a, String pw) throws UserNotExitsException {
        if (!StringUtils.hasText(a) || !StringUtils.hasText(pw))
            throw new UserNotExitsException("账号或密码不能为空");
        U user = getUser(a);
        return check(pw, user);
    }

    //获取 user 对象
    public U getUser(String a) throws UserNotExitsException {
        if (!StringUtils.hasText(a))
            throw new UserNotExitsException("账号不能为空");
        U user = cache.get(a);
        if (user == null){
            try {
                user = panel.getUser(a);
            }catch (Throwable e){
                throw new UsersException("获取用户: [" + a + "] 信息时发生异常", e);
            }

            if (user == null)
                throw new UserNotExitsException("账号不存在: [" + a + "]");

            //join cache
            cache.push(user.getAccount(), user);
        }
        return user;
    }

    //删除用户
    public boolean deleteUser(String a) throws UserNotExitsException {
        getUser(a);
        try {
            if (panel.deleteUser(a)) {
                cache.remove(a);
                return true;
            }
        }catch (Throwable e){
            throw new UsersException("删除用户: [" + a + "] 时发生异常", e);
        }
        return false;
    }

    //注册一名用户
    public boolean registerUser(U user){
        String a;
        if (!StringUtils.hasText(a = user.getAccount()) || !StringUtils.hasText(user.getPassword())){
            throw new UsersException("注册用户的账号或密码不能为空");
        }
        //加密密码
        user.setPassword(encrypt(user.getPassword()));
        try {
            getUser(a);
            throw new UsersException("注册得用户: [" + a + "] 已经存在");
        } catch (UserNotExitsException e) {}

        try {

            if (panel.joinUser(user)) {
                cache.push(a, user);
                return true;
            }
            return false;
        }catch (Throwable e){
            throw new UsersException("注册用户: [" + a + "] 发生异常", e);
        }
    }


    //更新用户信息
    public boolean updateUserAccountOrPassword(String oa, U newUser) throws UserNotExitsException {

        try {
            getUser(oa);
        } catch (UserNotExitsException e) {
            throw new UserNotExitsException("要修改得账号: [" + oa + "] 并不存在");
        }
        cache.wait(oa);
        try {
            try {
                String pwd;
                if (StringUtils.hasText(pwd = newUser.getPassword())) {
                    //如果需要更新密码, 则将密码进行加密
                    newUser.setPassword(encrypt(newUser.getPassword()));
                }

                if (panel.updateUser(oa, newUser)) {
                    cache.remove(oa);
                    cache.push(newUser.getAccount(), newUser);
                    return true;
                }
                return false;
            }catch (Throwable ex){
                throw new UsersException("更新用户: [" + oa + "] 信息时发生异常", ex);
            }
        }finally {
            cache.awaken(oa);
        }
    }

    public void clear(){
        cache.clear();
    }

}

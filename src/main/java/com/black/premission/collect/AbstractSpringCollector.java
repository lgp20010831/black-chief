package com.black.premission.collect;

import com.black.premission.*;
import com.black.core.util.Assert;
import com.black.user.UserPanel;
import org.springframework.beans.factory.BeanFactory;

public abstract class AbstractSpringCollector {

    public void collect(GlobalRUPConfiguration configuration){
        BeanFactory beanFactory = configuration.getSpringBeanFactory();
        Assert.notNull(beanFactory, "beanFactory is null");
        configuration.setPremissionPanel(beanFactory.getBean(PermissionPanel.class));
        configuration.setRolePanel(beanFactory.getBean(RolePanel.class));
        configuration.setRoleUserPanel(beanFactory.getBean(RoleUserPanel.class));
        configuration.setUserPanel(beanFactory.getBean(UserPanel.class));
        configuration.setRolePremissionPanel(beanFactory.getBean(RolePermissionPanel.class));
    }
}

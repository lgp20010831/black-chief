package com.black.premission;

import com.black.holder.SpringHodler;
import com.black.premission.collect.JpaCollector;
import com.black.premission.collect.MPCollector;
import com.black.premission.collect.MapSqlCollector;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.mvc.response.Response;
import com.black.core.spring.ApplicationHolder;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.Assert;
import com.black.premission.collect.SqlCollector;
import com.black.user.User;
import com.black.user.UserPanel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;

@Getter @Setter
public class GlobalRUPConfiguration {


    private RUPType rupType = RUPType.SQL;

    private RolePanel<Role> rolePanel;

    private PermissionPanel<Permission> premissionPanel;

    private RolePermissionPanel<RoleWithPermission> rolePremissionPanel;

    private UserPanel<User> userPanel;

    private RoleUserPanel<UserWithRole> roleUserPanel;

    private AliasColumnConvertHandler convertHandler;

    private String blendString;

    private BeanFactory springBeanFactory;

    private GlobalParentMapping parentMapping;

    private Class<? extends GlobalParentMapping> gpmType;

    private Class<? extends RestResponse> responseType = Response.class;

    public GlobalParentMapping getParentMapping() {
        if (parentMapping == null && rupType == RUPType.MAP_SQL){
            Assert.notNull(gpmType, "GlobalParentMapping type is null, can not find");
            findSpringBeanFactory();
            Assert.notNull(springBeanFactory, "springBeanFactory is null");
            parentMapping = springBeanFactory.getBean(gpmType);
        }
        return parentMapping;
    }

    public AliasColumnConvertHandler getConvertHandler() {
        if (convertHandler == null){
            convertHandler = new HumpColumnConvertHandler();
        }
        return convertHandler;
    }

    public void check(){
        Assert.notNull(rolePanel, "rolePanel is null");
        Assert.notNull(premissionPanel, "premissionPanel is null");
        Assert.notNull(rolePremissionPanel, "rolePremissionPanel is null");
        Assert.notNull(userPanel, "userPanel is null");
        Assert.notNull(roleUserPanel, "roleUserPanel is null");
        if (rupType == RUPType.MAP_SQL && getParentMapping() == null){
            throw new IllegalStateException("parentMapping is null");
        }
    }

    public void load(){
        findSpringBeanFactory();
        switch (rupType){
            case MAP_SQL:
                MapSqlCollector collector = new MapSqlCollector();
                collector.collect(this);
                break;
            case JRA:
                JpaCollector jpaCollector = new JpaCollector();
                jpaCollector.collect(this);
                break;
            case MYBATIS_PLUS:
                MPCollector mpCollector = new MPCollector();
                mpCollector.collect(this);
                break;
            case SQL:
                SqlCollector sqlCollector = new SqlCollector();
                sqlCollector.collect(this);
                break;
            case CUSTOM:
                break;
            default:
                throw new IllegalStateException("ill state:" + rupType);
        }
    }

    private void findSpringBeanFactory(){
        if (springBeanFactory != null){
            return;
        }

        BeanFactory beanFactory = ApplicationHolder.getBeanFactory();
        if (beanFactory != null){
            springBeanFactory = beanFactory;
            return;
        }

        beanFactory = SpringHodler.getBeanFactory();
        if (beanFactory != null){
            springBeanFactory = beanFactory;
        }
    }
}

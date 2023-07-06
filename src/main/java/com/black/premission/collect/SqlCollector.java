package com.black.premission.collect;

import com.black.GlobalVariablePool;
import com.black.core.json.Trust;
import com.black.function.Consumer;
import com.black.javassist.*;
import com.black.premission.*;
import com.black.premission.sql.*;
import com.black.sql_v2.Sql;
import com.black.template.jdbc.JavaColumnMetadata;
import com.black.user.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("all")
public class SqlCollector extends AbstractSpringCollector{




    @Override
    public void collect(GlobalRUPConfiguration configuration) {
        //根据数据库表生成动态虚拟类
        RUPEntityManager instance = RUPEntityManager.getInstance();
        Class<? extends Permission> permissionEntity = createPermissionEntity(GlobalVariablePool.RUP_MS_PERMISSION_TABLE_NAME);
        instance.setPermissionEntity(permissionEntity);
        Class<? extends Role> roleEntity = createRoleEntity(GlobalVariablePool.RUP_MS_ROLE_TABLE_NAME);
        instance.setRoleEntity(roleEntity);
        Class<? extends User> userEntity = createUserEntity(GlobalVariablePool.RUP_MS_USER_TABLE_NAME);
        instance.setUserEntity(userEntity);
        Class<? extends UserWithRole> userRoleEntity = createUserRoleEntity(GlobalVariablePool.RUP_MS_USER_ROLE_TABLE_NAME);
        instance.setUserWithRoleEntity(userRoleEntity);
        Class<? extends RoleWithPermission> rolePermissionEntity = createRolePermissionEntity(GlobalVariablePool.RUP_MS_ROLE_PERMISSION_TABLE_NAME);
        instance.setRoleWithPermissionEntity(rolePermissionEntity);
        configuration.setPremissionPanel(new SqlPermissionPanel<Permission>() {});
        configuration.setRolePanel(new SqlRolePanel<Role>() {});
        configuration.setUserPanel(new SqlUserPanel<User>() {});
        configuration.setRolePremissionPanel(new SqlRoleWithPermissionPanel<RoleWithPermission>() {});
        configuration.setRoleUserPanel(new SqlUserWithRolePanel<UserWithRole>() {});

    }

    private static Class<? extends RoleWithPermission> createRolePermissionEntity(String tableName){
        return (Class<? extends RoleWithPermission>) createDynamicClass(tableName, RoleWithPermissionRewriteBean.class);
    }

    private static Class<? extends UserWithRole> createUserRoleEntity(String tableName){
        return (Class<? extends UserWithRole>) createDynamicClass(tableName, UserWithRoleRewriteBean.class);
    }


    private static Class<? extends Role> createRoleEntity(String tableName){
        return (Class<? extends Role>) createDynamicClass(tableName, RoleRewriteBean.class);
    }

    private static Class<? extends User> createUserEntity(String tableName){
        return (Class<? extends User>) createDynamicClass(tableName, UserRewriteBean.class);
    }

    private static Class<? extends Permission> createPermissionEntity(String tableName){
        return (Class<? extends Permission>) createDynamicClass(tableName, PermissionRewriteBean.class);
    }

    private static Class<?> createDynamicClass(String tableName, Class<?> superClass){
        JdbcEntityCreator entityCreator = new JdbcEntityCreator();
        entityCreator.setConnection(Sql.opt().getConnection());
        entityCreator.setColumnAnnotationGenerator(new ColumnAnnotationGenerator() {
            @Override
            public CtAnnotations createAnnotations(JavaColumnMetadata javaColumnMetadata) {
                CtAnnotation apiModel = new CtAnnotation(ApiModelProperty.class);
                apiModel.addField("value", javaColumnMetadata.getRemarks(), String.class);
                return CtAnnotations.group(apiModel);
            }
        });
        entityCreator.setPartiallyCtClassConsumer(new Consumer<PartiallyCtClass>() {
            @Override
            public void accept(PartiallyCtClass partiallyCtClass) throws Throwable {
                CtAnnotation trust = new CtAnnotation(Trust.class);
                partiallyCtClass.addClassAnnotations(CtAnnotations.group(trust));
                partiallyCtClass.setSuperClass(superClass);
            }
        });
        try {
            return entityCreator.createJavaClass(tableName);
        }finally {
            entityCreator.close();
        }

    }

    @Getter @Setter
    public static class RUPEntityManager{

        private static RUPEntityManager manager;

        public synchronized static RUPEntityManager getInstance() {
            if (manager == null){
                manager = new RUPEntityManager();
            }
            return manager;
        }

        private Class<? extends Permission> permissionEntity;
        private Class<? extends User> userEntity;
        private Class<? extends Role> roleEntity;
        private Class<? extends UserWithRole> userWithRoleEntity;
        private Class<? extends RoleWithPermission> roleWithPermissionEntity;

    }
}

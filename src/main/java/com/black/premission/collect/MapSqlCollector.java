package com.black.premission.collect;

import com.black.premission.*;
import com.black.premission.map_sql.*;
import com.black.user.UserPanel;

public class MapSqlCollector {


    public void collect(GlobalRUPConfiguration configuration){
        UserPanel userPanel = new MSUserPanel(){};
        configuration.setUserPanel(userPanel);
        RolePermissionPanel msRolePermissionPanel = new MSRolePermissionPanel() {};
        configuration.setRolePremissionPanel(msRolePermissionPanel);
        RolePanel msRolePanel = new MSRolePanel() {};
        configuration.setRolePanel(msRolePanel);
        PermissionPanel permissionPanel = new MSPermissionPanel() {};
        configuration.setPremissionPanel(permissionPanel);
        RoleUserPanel roleUserPanel = new MSUserRolePanel(){};
        configuration.setRoleUserPanel(roleUserPanel);
    }

}

package com.black;

//常量池
public class GlobalVariablePool {

    public static Integer HTTP_CODE_SUCCESSFUL = 200;

    public static Integer HTTP_CODE_SYSTEM_ERROR = 500;

    public static Integer HTTP_CODE_NO_PERMISSION = 408;

    public static Integer HTTP_CODE_TOKEN_EXPIRATION = 451;

    public static Integer HTTP_CODE_TOKEN_INVAILD = 453;

    public static Integer HTTP_CODE_NO_TOKEN = 472;

    public static Integer HTTP_CODE_CURRENT_lIMITING = 546;

    public static String HTTP_MSG_SUCCESSFUL = "操作成功";

    public static String HTTP_MSG_FAIL = "操作失败";

    public static String HTTP_MSG_TOKEN_EXPIRATION = "会话过期, 请重新登录";

    public static String HTTP_MSG_TOKEN_INVAILD = "会话无效, 请重新登录";

    public static String HTTP_MSG_NO_TOKEN = "无权限访问";

    public static String HTTP_MSG_CURRENT_lIMITING = "服务器繁忙, 请稍后再试";


    //------------------------------------------------------
    //              R  U  P
    //------------------------------------------------------

    public static String RUP_ID_NAME = "id";

    public static String RUP_ROLE_ID_NAME = "roleId";

    public static String RUP_PERMISSION_ID_NAME = "permissionId";

    public static String RUP_USER_ID_NAME = "userId";

    public static String RUP_NAME = "name";

    public static String RUP_ROLE_LIST_NAME = "roles";

    public static String RUP_USER_ACCOUNT_NAME = "account";

    public static String RUP_USER_PASSWORD_NAME = "password";

    public static String RUP_PERMISSION_LIST_NAME = "permissions";

    public static String RUP_PERMISSION_FATHER_NAME = "parent";

    public static String RUP_PERMISSION_CHILDREN_NAME = "children";

    public static String RUP_MS_ROLE_TABLE_NAME = "role";

    public static String RUP_MS_ROLE_PERMISSION_TABLE_NAME = "role_permission";

    public static String RUP_MS_PERMISSION_TABLE_NAME = "permission";

    public static String RUP_MS_USER_TABLE_NAME = "user_info";

    public static String RUP_MS_USER_ROLE_TABLE_NAME = "role_user";

}

package com.black.sql_v2.action;

import com.black.api.ApiRemark;
import com.black.core.annotation.ChiefServlet;
import lombok.Getter;

@Getter @ChiefServlet("user") @ApiRemark("用户管理")
public class UserController extends AbstractProvideSupportChiefApiController{

    String tableName = "user";

}

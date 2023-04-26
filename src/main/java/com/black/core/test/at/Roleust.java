package com.black.core.test.at;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.black.api.ApiRemark;
import com.black.core.json.Trust;
import com.black.core.tools.BaseBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel
@TableName("role")
@Data @Trust @SuppressWarnings("all")
public class Roleust extends BaseBean<Roleust>{

    private static final long serialVersionUID = 1L;

 
    @ApiModelProperty("角色名称")
    @TableField(value = "role_name")
    @ApiRemark("角色名称")
    private String roleName;

    @ApiModelProperty("更新者")
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    @ApiRemark("更新者") 
    private Integer updateUser;

    @ApiModelProperty("集团ID")
    @TableField(value = "group_id")
    @ApiRemark("集团ID") 
    private Integer groupId;

    @ApiModelProperty("角色ID")
    @TableField(value = "role_id")
    @ApiRemark("角色ID") 
    private Integer roleId;

    @ApiModelProperty("诊所ID")
    @TableField(value = "branch_id")
    @ApiRemark("诊所ID") 
    private Integer branchId;

    @ApiModelProperty("角色说明")
    @TableField(value = "remark")
    @ApiRemark("角色说明") 
    private String remark;

    @ApiModelProperty("创建者")
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    @ApiRemark("创建者") 
    private Integer createUser;

    @ApiModelProperty("角色分类：0：集团角色1：分院角色")
    @TableField(value = "type")
    @ApiRemark("角色分类：0：集团角色1：分院角色") 
    private String type;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @ApiRemark("创建时间") 
    private Date createDate;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_date", fill = FieldFill.INSERT_UPDATE)
    @ApiRemark("更新时间") 
    private Date updateDate;

  
}
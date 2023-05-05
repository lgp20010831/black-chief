package com.black.core.yml;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.black.core.json.Trust;
import com.black.ibtais.IbatisBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@TableName("supplier")
@Data @Trust @SuppressWarnings("all")
public class Supplier extends IbatisBean<Supplier>{

    private static final long serialVersionUID = 1L;

 
    @ApiModelProperty("")
    @TableField(value = "id") @TableId
    private String id;

    @ApiModelProperty("")
    @TableField(value = "code")
    private String code;

    @ApiModelProperty("")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("")
    @TableField(value = "short_name")
    private String shortName;

    @ApiModelProperty("税号")
    @TableField(value = "tax_number")
    private String taxNumber;

    @ApiModelProperty("")
    @TableField(value = "supplier_type_id")
    private String supplierTypeId;

    @ApiModelProperty("开业日期")
    @TableField(value = "open_date")
    private Date openDate;

    @ApiModelProperty("注册资本")
    @TableField(value = "registered_capital")
    private Double registeredCapital;

    @ApiModelProperty("员工人数")
    @TableField(value = "emp_count")
    private Integer empCount;

    @ApiModelProperty("法人")
    @TableField(value = "corporate")
    private String corporate;

    @ApiModelProperty("电话")
    @TableField(value = "phone")
    private String phone;

    @ApiModelProperty("交易币种")
    @TableField(value = "currency")
    private String currency;

    @ApiModelProperty("审核周期")
    @TableField(value = "audit_cycle")
    private String auditCycle;

    @ApiModelProperty("审核状态 1:已核准 2:尚待核准 3:不准交易 4:黑名单")
    @TableField(value = "approval_state")
    private String approvalState;

    @ApiModelProperty("邮箱地址")
    @TableField(value = "mail")
    private String mail;

    @ApiModelProperty("")
    @TableField(value = "purchase")
    private String purchase;

    @ApiModelProperty("状态")
    @TableField(value = "state")
    private String state;

    @ApiModelProperty("")
    @TableField(value = "mark")
    private String mark;

    @ApiModelProperty("")
    @TableField(value = "deleted_at")
    private Timestamp deletedAt;

    @ApiModelProperty("")
    @TableField(value = "inserted_at", fill = FieldFill.INSERT)
    private Timestamp insertedAt;

    @ApiModelProperty("")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Timestamp updatedAt;

    @ApiModelProperty("")
    @TableField(value = "is_deleted")
    private Boolean isDeleted;

    @ApiModelProperty("")
    @TableField(value = "shortcut_code")
    private String shortcutCode;

    @ApiModelProperty("")
    @TableField(value = "arrival_days")
    private Integer arrivalDays;

    @ApiModelProperty("付款方式")
    @TableField(value = "procurement_method")
    private String procurementMethod;

    @ApiModelProperty("是否天天送货 0:否 1:是")
    @TableField(value = "delivery_everyday")
    private String deliveryEveryday;

  
}
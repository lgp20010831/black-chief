package com.black.core.yml.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.black.core.json.Trust;
import com.black.ibtais.IbatisBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@TableName("ayc")
@Data @Trust @SuppressWarnings("all")
public class Ayc extends IbatisBean<Ayc>{

    private static final long serialVersionUID = 1L;

 
    @ApiModelProperty("")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("")
    @TableField(value = "id") @TableId
    private Integer id;

    @ApiModelProperty("")
    @TableField(value = "age")
    private Integer age;

  
}
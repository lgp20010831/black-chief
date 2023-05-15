package ${location.generatePath};

import lombok.Data;
import com.black.ibtais.IbatisBean;
import com.black.core.json.Trust;
import io.swagger.annotations.ApiModelProperty;
import com.baomidou.mybatisplus.annotation.*;

import java.math.*;
import java.util.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@TableName("${source.tableName}")
@Data @Trust @SuppressWarnings("all")
public class ${source.className} extends IbatisBean<${source.className}>{

    private static final long serialVersionUID = 1L;

<#list source.tableMetadata.columnMetadatas as field>
    <#if inSwagger>@ApiModelProperty("${field.remarks}")</#if>
    <#assign isId=false/>
    <#list source.primaryKeys as pks>
        <#if pks=field.name>
            <#assign isId=true/>
    @TableId
            <#break />
        </#if>
    </#list>
    <#if isId=false>
    ${field.fieldAnnotation}
    </#if>
    private ${field.javaTypeName} ${field.javaFieldName};

</#list>
}
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
    @ApiModelProperty("${field.remarks}")
    ${field.fieldAnnotation}<#if field.javaFieldName == 'id'>@TableId</#if>
    private ${field.javaTypeName} ${field.javaFieldName};
</#list>
}
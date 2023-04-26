package com.black.nest;

import com.black.core.json.Trust;
import com.black.utils.IdUtils;
import com.black.utils.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Trust
@NoArgsConstructor @AllArgsConstructor
public class Dict {

    private String id;

    private String dictTableName;

    private String sourceTableName;

    //p_code =xxx
    private String pCodeName;

    private String pCodeValue;

    //code =
    private String codeName;

    //r.xxx
    private String sourceFieldName;

    //取字典表哪个字段
    private String resultName;

    //替换成别名是什么
    private String resultNameAlias;

    private String createdAt;

    public Dict fill(){
        id = IdUtils.createShort8Id();
        createdAt = ServiceUtils.now();
        return this;
    }
}

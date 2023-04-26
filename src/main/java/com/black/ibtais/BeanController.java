package com.black.ibtais;

import com.black.core.json.ReflexUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.ParentController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class BeanController extends ParentController {

    public  <T> T toBean(Map<String, Object> map, Class<T> type){
        return BeanUtil.mapping(ReflexUtils.instance(type), map);
    }


    public <T> List<T> toBeanBatch(Collection<Object> collection, Class<T> type){
        return BeanUtil.toBeanBatch(collection, type);
    }
}

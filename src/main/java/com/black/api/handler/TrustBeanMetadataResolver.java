package com.black.api.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiRemark;
import com.black.api.ApiV2Utils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import static com.black.api.ApiV2Utils.*;

public class TrustBeanMetadataResolver implements MetadataResolver{


    public static final LinkedBlockingQueue<Function<FieldWrapper, String>> obtainRemarkQueue = new LinkedBlockingQueue<>();

    static {
        obtainRemarkQueue.add(fw -> {
            ApiRemark annotation = fw.getAnnotation(ApiRemark.class);
            return annotation == null ? null : annotation.value();
        });
    }

    @Override
    public boolean support(Object metadata) {
        return metadata instanceof Class;
    }

    @Override
    public void resolve(Object metadata, JSONObject sonJson, AliasColumnConvertHandler handler, boolean request) {
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(metadata);
        Collection<FieldWrapper> fields = classWrapper.getFields();
        for (FieldWrapper fieldWrapper : fields) {
            String name = fieldWrapper.getName();
            Class<?> type = fieldWrapper.getType();
            String cloumn = handler.convertAlias(name);
            String remark = null;
            if (request && requestExcludes.contains(cloumn)){
                continue;
            }
            if (Collection.class.isAssignableFrom(type)){
                Class<?>[] genericVal = ReflectionUtils.genericVal(fieldWrapper.getField(), type);
                if (genericVal.length == 1){
                    Class<?> arrayed = ServiceUtils.arrayIndex(genericVal, 0);
                    if (!arrayed.equals(classWrapper.get())){
                        String simpleName = arrayed.getSimpleName();
                        Object data = getMetadata(simpleName, null);
                        if (data != null){
                            JSONObject jsonObject = new JSONObject();
                            ApiV2Utils.processorJson(data, jsonObject, handler, request);
                            sonJson.put(name, jsonObject);
                            continue;
                        }
                    }
                }

            }

            for (Function<FieldWrapper, String> function : obtainRemarkQueue) {
                remark = function.apply(fieldWrapper);
                if (remark != null){
                    break;
                }
            }
            if(remark == null){
                remark = "";
            }
            if (remarkJoin && StringUtils.hasText(remark)){
                wriedRemark(sonJson, name, type, remark, null, request);
            }else {
                if (type.equals(Boolean.class)){
                    writeBoolean(sonJson, name);
                }else if (Number.class.isAssignableFrom(type)){
                    wriedInt(sonJson, name);
                }else if ((Date.class.equals(type) || Time.class.equals(type) || Timestamp.class.equals(type) || LocalDateTime.class.equals(type))){
                    wriedDate(sonJson, name);
                }else {
                    wriedString(sonJson, name, type);
                }
            }
        }
    }
}

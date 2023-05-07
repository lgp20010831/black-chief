package com.black.ibtais;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.tools.BaseBean;
import com.black.core.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Transient;
import java.util.List;

@SuppressWarnings("all")
public class IbatisBean <T> extends BaseBean<T> {

    @Transient
    @TableField(exist = false)
    @ApiModelProperty(hidden = true)
    protected BaseMapper<T> mybatisPlusBaseMapper;

    @Transient
    @TableField(exist = false)
    @ApiModelProperty(hidden = true)
    protected IService<T> mybatisPlusService;

    public T object(){
        return (T) this;
    }

    public BaseMapper<T> obtainBaseMapper(){
        if (mybatisPlusBaseMapper == null){
            mybatisPlusBaseMapper = (BaseMapper<T>) IbtatisUtils.autoFindMapper(getClass());
            if (mybatisPlusBaseMapper == null){
                throw new IllegalStateException("can not find base mapper of entity: " + getClass());
            }
        }
        return mybatisPlusBaseMapper;
    }

    public IService<T> obtainIService(){
        if (mybatisPlusService == null){
            mybatisPlusService = (IService<T>) IbtatisUtils.autoFindService(getClass());
            if (mybatisPlusService == null){
                throw new IllegalStateException("can not find service of entity: " + getClass());
            }
        }
        return mybatisPlusService;
    }

    public String obatinTableName(){
        Class<? extends IbatisBean> type = getClass();
        TableName annotation = type.getAnnotation(TableName.class);
        if (annotation != null){
            return annotation.value();
        }
        HumpColumnConvertHandler handler = new HumpColumnConvertHandler();
        return handler.convertColumn(StringUtils.titleLower(type.getSimpleName()));
    }


    public T save(){
        obtainIService().save(object());
        return object();
    }

    public T saveOrUpdate(){
        obtainIService().saveOrUpdate(object());
        return object();
    }

    public boolean removeById(){
        return obtainIService().removeById(object());
    }

    public T updateById(){
        obtainIService().updateById(object());
        return object();
    }

    public T one(){
        return one((String) null);
    }

    public T one(String blend){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        AbstractWrapper<T, String, ?> wriedWrapper = IbtatisUtils.wriedWrapper(wrapper, object(), blend);
        return one(wriedWrapper);
    }

    public T one(Wrapper<T> queryWrapper){
        return obtainIService().getOne(queryWrapper);
    }

    public long count(){
        return count((String) null);
    }

    public long count(String blend){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        AbstractWrapper<T, String, ?> wriedWrapper = IbtatisUtils.wriedWrapper(wrapper, object(), blend);
        return count(wriedWrapper);
    }

    public long count(Wrapper<T> queryWrapper){
        return obtainIService().count(queryWrapper);
    }


    public List<T> list(){
        return list((String) null);
    }

    public List<T> list(String blend){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        AbstractWrapper<T, String, ?> wriedWrapper = IbtatisUtils.wriedWrapper(wrapper, object(), blend);
        return list(wriedWrapper);
    }

    public List<T> list(Wrapper<T> queryWrapper){
        return obtainIService().list(queryWrapper);
    }


    public <E extends IPage<T>> E page(E page){
        return page(page, (String) null);
    }

    public <E extends IPage<T>> E page(E page, String blend){
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        AbstractWrapper<T, String, ?> wriedWrapper = IbtatisUtils.wriedWrapper(wrapper, object(), blend);
        return page(page, wriedWrapper);
    }

    public <E extends IPage<T>> E page(E page, Wrapper<T> queryWrapper){
        return obtainIService().page(page, queryWrapper);
    }
}

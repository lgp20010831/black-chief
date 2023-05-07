package com.black.core.yml.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.core.yml.pojo.Ayc;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AycMapper extends BaseMapper<Ayc> {
}

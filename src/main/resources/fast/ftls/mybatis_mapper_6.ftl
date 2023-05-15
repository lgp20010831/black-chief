package ${location.generatePath};

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${pojoPath}.${source.className};
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ${source.className}Mapper extends BaseMapper<${source.className}> {
}

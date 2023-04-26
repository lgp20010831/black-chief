package com.black.premission.jpa;

import com.black.premission.Attribute;
import com.black.premission.EntityPanel;
import com.black.premission.Panel;
import com.black.core.json.ReflexUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Map;

public interface JPAPanel<R extends Attribute> extends JpaRepository<R, String>, JpaSpecificationExecutor<R>, Panel<R>, EntityPanel<R> {

    @Override
    default R findDataById(String id){
        return findById(id).orElse(null);
    }

    @Override
    default List<R> dataList(R condition){
        return findAll(Example.of(condition));
    }

    @Override
    default R join(R r){
        return save(r);
    }

    @Override
    default R dataSave(R r){
        return save(r);
    }

    @Override
    default boolean deleteData(String id){
        deleteById(id);
        return true;
    }

    default R instance(Map<String, Object> condition){
        R instance = ReflexUtils.instance(entityType());
        instance.putAll(condition);
        return instance;
    }

}

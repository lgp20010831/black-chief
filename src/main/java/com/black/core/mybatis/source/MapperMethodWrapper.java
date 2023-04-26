package com.black.core.mybatis.source;

import org.apache.ibatis.session.SqlSession;

public interface MapperMethodWrapper {

    Object execute(SqlSession sqlSession, Object[] args);
}

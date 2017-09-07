package com.ott.canal.domain;

/**
 * 引入jar之后要实现此接口
 */
public interface SqlCallack {
    String writeSql(String dbName,String sqlStr);
}

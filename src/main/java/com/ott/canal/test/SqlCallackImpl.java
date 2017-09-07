package com.ott.canal.test;

import com.ott.canal.domain.SqlCallack;

/**
 * Created by dongcunxiao on 2017/8/30 9:06
 */
public class SqlCallackImpl implements SqlCallack {
    @Override
    public String writeSql(String dbName, String sqlStr) {
        System.out.println("数据库名字 : "+dbName + "   SQL : " + sqlStr);
        return null;
    }
}

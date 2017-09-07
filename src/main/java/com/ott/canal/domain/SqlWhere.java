package com.ott.canal.domain;

import java.util.Map;

/**
 * Created by dongcunxiao on 2017/9/6 14:43
 */
class SqlWhere {
    // 默认有主键
    private boolean haveKey = true;
    // 条件语句
    private String whereStr;

    // update语句中中set后key=val
    private Map<String,String> keyValMap;

    public boolean isHaveKey() {
        return haveKey;
    }

    public void setHaveKey(boolean haveKey) {
        this.haveKey = haveKey;
    }

    public String getWhereStr() {
        return whereStr;
    }

    public void setWhereStr(String whereStr) {
        this.whereStr = whereStr;
    }

    public Map<String, String> getKeyValMap() {
        return keyValMap;
    }
    public void setKeyValMap(Map<String, String> keyValMap) {
        this.keyValMap = keyValMap;
    }
}

//8-6
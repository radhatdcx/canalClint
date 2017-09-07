package com.ott.canal.domain;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.ott.canal.config.CanalConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceThread implements Runnable {

    // canal 连接
    private CanalConnector connector = null;

    // 回调方法
    private SqlCallack sqlCallack = null;

    public ServiceThread(CanalConnector connector,SqlCallack sqlCallack) {
        this.connector = connector;
        this.sqlCallack = sqlCallack;
    }
    @Override
    public void run() {
        System.out.println("==========线程中run方法执行============");
        try {
            // 打开连接
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            this.work();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
    }

    // 循环获取
    private void work() {
        System.out.println("==========线程中work方法执行============");
        boolean flag = true;
        while (flag) {
            try {
                int batchSize = CanalConfig.batchSize == null ? 1000 : CanalConfig.batchSize;
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    System.out.println("============数据库没有变更============" );
                    try {
                        Thread.sleep(CanalConfig.sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    printEntry(message.getEntries());
                }
                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printEntry( List<CanalEntry.Entry> entrys) {

        // 数据库执行的每条sql （每条sql可能影响多条数据）
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }
            System.out.println("++++++++++++++++++++entry+++++++++++++++++++++++==");
            workSql(entry,rowChage);
        }
    }

    private void workSql(CanalEntry.Entry entry, CanalEntry.RowChange rowChage) {
        // SQL 语句类型
        CanalEntry.EventType eventType = rowChage.getEventType();
        // 数据库名字
        String schemaName = entry.getHeader().getSchemaName();
        // 表名字
        String tableName = entry.getHeader().getTableName();

        System.out.println("+++++rowChage.getRowDatasList()  :  +++++" +  rowChage.getRowDatasList().size() + "++++++++");
        // 一条sql影响几条数据
        for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
            if (eventType == CanalEntry.EventType.DELETE) {
                SqlWhere sqlWhere = deletColumn(rowData);
                // 如果有主键
                if(sqlWhere.isHaveKey()) {
                    // 删除
                    String deleteSql = "DELETE FROM  " + schemaName + "." + tableName + " WHERE " + sqlWhere.getWhereStr();
                    // 回调方法
                    callBak(tableName,deleteSql);
                }else {
                    System.out.println("DELETE操作" + schemaName + "." + tableName + "： 不支持没有主键的SQL");
                }
            } else if (eventType == CanalEntry.EventType.INSERT) {
                Map<String,String> map = insertColumn(rowData);
                // 插入
                String insertSql = "INSERT INTO " + schemaName + "." + tableName + "(" + map.get("key") + ")" + " VALUES " + "(" + map.get("val") + ")";
                // 回调方法
                callBak(tableName,insertSql);
            } else  if (eventType == CanalEntry.EventType.UPDATE) {
                SqlWhere updateWhere = updateColumn(rowData);
                Map<String,String> updateMap = updateWhere.getKeyValMap();
                // 如果有主键
                if(updateWhere.isHaveKey()) {
                    // 更新
                    String updateSql = "UPDATE " + schemaName + "." + tableName + " SET " + updateMap.get("sqlStr") + " WHERE " + updateMap.get("keyStr");
                    // 回调方法
                    callBak(tableName,updateSql);
                }else {
                    System.out.println("UPDATE操作" + schemaName + "." + tableName + "： 不支持没有主键的SQL");
                }
            }
        }
    }


    // 拼接Update 语句列
    private  SqlWhere updateColumn(CanalEntry.RowData rowData) {
        //UPDATE table001 SET name = 'dcx',age='12' WHERE name = '213123'
        StringBuilder sqlStr = new StringBuilder();
        List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
        // 当前sql语句的 主键 默认id
        String keyStr = null;
        for (int i = 0;i < columns.size();i++) {
            //System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
            CanalEntry.Column column = columns.get(i);
            sqlStr.append(column.getName());
            sqlStr.append(" = ");
            sqlStr.append("'" + column.getValue()+"'");
            // 判断当前列是否是主键
            if(column.getIsKey()) {
                keyStr = column.getName() + " = " + column.getValue();
            }
            if(i < columns.size() - 1) {
                sqlStr.append(",");
            }
        }
        SqlWhere sqlWhere = new SqlWhere();
        Map<String,String> map = new HashMap<>();
        map.put("sqlStr",sqlStr.toString());
        // 那一列是主键
        map.put("keyStr",keyStr);
        sqlWhere.setKeyValMap(map);
        if(keyStr == null) {
            sqlWhere.setHaveKey(false);
        }
        return sqlWhere;
    }

    // 删除列拼接
    private SqlWhere deletColumn(CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columns = rowData.getBeforeColumnsList();
        SqlWhere sqlWhere = new SqlWhere();
        String whereStr = "";
        // 每一列
        for (int i = 0;i < columns.size();i++) {
            CanalEntry.Column column = columns.get(i);
            // 当前列是主键
            if(column.getIsKey()) {
                String columnName = column.getName();
                String columnVal = column.getValue();
                whereStr = columnName + " = " + "'" + columnVal + "'";
                sqlWhere.setWhereStr(whereStr);
            }else {
                // 没有主键
                sqlWhere.setHaveKey(false);
            }
        }
        return sqlWhere;
    }

    // 插入拼接
    private Map<String,String> insertColumn(CanalEntry.RowData rowData) {
        //INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)
        List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
        StringBuilder key =  new StringBuilder();
        StringBuilder val = new StringBuilder();
        for (int i = 0;i < columns.size();i++) {
            CanalEntry.Column column = columns.get(i);
            if(column.getValue() != null && !"".equals(column.getValue())) {
                key.append(column.getName());
                val.append(column.getValue());
                key.append(",");
                val.append(",");
            }
        }
        Map<String,String> map = new HashMap<>();
        map.put("key",key.toString().substring(0,key.toString().length()-1));
        map.put("val",val.toString().substring(0,val.toString().length()-1));
        return map;
    }
    // 回调
    private void callBak(String dbName,String sqlStr) {
        sqlCallack.writeSql(dbName,sqlStr);
    }
}




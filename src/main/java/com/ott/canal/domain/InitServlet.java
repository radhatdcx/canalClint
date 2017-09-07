package com.ott.canal.domain;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.ott.canal.config.CanalConfig;
import javax.servlet.http.HttpServlet;
import java.net.InetSocketAddress;

public class InitServlet extends HttpServlet {

    public InitServlet(SqlCallack sqlCallack) {
        this.sqlCallack = sqlCallack;
        System.out.println("===========InitServlet 构造方法初始化===========");
    }
    public InitServlet() {
    }
    // 需要在初始化时候注入
    public SqlCallack sqlCallack;
    @Override
    public void init(){
        System.out.println("===========servlet 中 init调用==========");
        String hostName = CanalConfig.hostName;
        Integer port = CanalConfig.port;
        String destination = CanalConfig.distination;
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostName,
                port), destination, "", "");
        new Thread(new ServiceThread(connector,sqlCallack)).start();
    }
}
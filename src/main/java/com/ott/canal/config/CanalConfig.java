package com.ott.canal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
public class CanalConfig {
    // canal中定义的ip地址 需要和canal配置文件中的IP对应
    public static String hostName = "localhost";
    // canal中定义的端口 需要和canal配置文件中的端口对应
    public static Integer port = 11111;
    // 实例名字
    public static String distination = "example";
    // canal 用户名
    public static String userName;
    // canal 密码
    public static String passWord;
    // 每次从canal中get 多少条数据
    public static Integer batchSize = 200;
    // 每次get 间隔多长时间 （单位 毫秒）
    public static Integer sleepTime = 40000;
}

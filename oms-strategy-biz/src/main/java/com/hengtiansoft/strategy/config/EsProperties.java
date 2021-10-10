package com.hengtiansoft.strategy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "elasticsearch")
public class EsProperties {

    private String hosts = "127.0.0.1"; // ES请求地址
    private int port = 9200; //端口
    private String protocol = "http"; //协议

    private int connectTimeOut = 1000; // 连接超时时间
    private int socketTimeOut = 30000; // 连接超时时间
    private int connectionRequestTimeOut = 500; // 获取连接的超时时间

    private int maxConnectNum = 100; // 最大连接数
    private int maxConnectPerRoute = 100; // 最大路由连接数

}
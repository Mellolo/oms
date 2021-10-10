package com.hengtiansoft.strategy.config;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@EnableConfigurationProperties(EsProperties.class)
public class EsAutoConfiguration {

    private EsProperties properties;
    private ArrayList<HttpHost> hostList;

    public EsAutoConfiguration(EsProperties properties) {
        this.properties = properties;
        hostList = new ArrayList<>();
        String[] hostStrs = properties.getHosts().split(",");
        for (String host : hostStrs) {
            hostList.add(new HttpHost(host, properties.getPort(), properties.getProtocol()));
        }
    }

    @Bean
    public RestHighLevelClient client() {
        RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(properties.getConnectTimeOut());
                requestConfigBuilder.setSocketTimeout(properties.getSocketTimeOut());
                requestConfigBuilder.setConnectionRequestTimeout(properties.getConnectionRequestTimeOut());
                return requestConfigBuilder;
            }
        });
        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(properties.getMaxConnectNum());
                httpClientBuilder.setMaxConnPerRoute(properties.getMaxConnectPerRoute());
                return httpClientBuilder;
            }
        });
        return new RestHighLevelClient(builder);
    }

}

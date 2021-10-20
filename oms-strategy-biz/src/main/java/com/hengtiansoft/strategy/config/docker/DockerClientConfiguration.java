package com.hengtiansoft.strategy.config.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(DockerClientProperties.class)
public class DockerClientConfiguration {

    private DockerClientProperties properties;

    public DockerClientConfiguration(DockerClientProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DockerClientConfig dockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(properties.getDockerHost())
                .build();
    }

    @Bean
    public DockerHttpClient dockerHttpClient() {
        DockerClientConfig config = dockerClientConfig();
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(properties.getMaxConnections())
                .connectionTimeout(Duration.ofSeconds(properties.getConnectionTimeout()))
                .responseTimeout(Duration.ofSeconds(properties.getResponseTimeout()))
                .build();
    }

    @Bean
    public DockerClient dockerClient() {
        return DockerClientImpl.getInstance(dockerClientConfig(), dockerHttpClient());
    }
}

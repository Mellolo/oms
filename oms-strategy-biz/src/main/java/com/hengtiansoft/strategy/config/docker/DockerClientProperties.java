package com.hengtiansoft.strategy.config.docker;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "docker.client")
public class DockerClientProperties {

    private String dockerHost;
    private int maxConnections = Integer.MAX_VALUE;
    private int connectionTimeout = 30;
    private int responseTimeout = 45;


}

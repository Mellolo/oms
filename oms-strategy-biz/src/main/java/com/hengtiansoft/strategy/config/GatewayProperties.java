package com.hengtiansoft.strategy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "py4j")
public class GatewayProperties {

    private int port = 25333; //端口
}

package com.hengtiansoft.strategy.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dynamic")
public class DynamicRouteProperties {
    private boolean enabledDynamicRoute;
}

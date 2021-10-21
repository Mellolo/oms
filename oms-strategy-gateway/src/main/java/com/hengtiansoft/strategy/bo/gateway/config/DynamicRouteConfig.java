package com.hengtiansoft.strategy.bo.gateway.config;

import com.hengtiansoft.strategy.bo.gateway.Router.DynamicRouter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(DynamicRouteProperties.class)
@Configuration
public class DynamicRouteConfig {

    private DynamicRouteProperties properties;

    public DynamicRouteConfig(DynamicRouteProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SimpleRouteLocator routeLocator(ZuulProperties zuulProperties) {
        return new DynamicRouter(properties.isEnabledDynamicRoute(), zuulProperties.getPrefix(), zuulProperties);
    }
}

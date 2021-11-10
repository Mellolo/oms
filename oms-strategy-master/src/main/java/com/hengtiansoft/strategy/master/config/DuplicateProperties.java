package com.hengtiansoft.strategy.master.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "duplicate")
public class DuplicateProperties {
    private int num = 1;
}

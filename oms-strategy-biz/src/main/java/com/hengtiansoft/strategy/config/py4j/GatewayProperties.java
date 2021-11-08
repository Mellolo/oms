package com.hengtiansoft.strategy.config.py4j;

import com.hengtiansoft.strategy.utils.HostPortUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "py4j")
public class GatewayProperties {

    private int port = 25333; //端口
    private int pythonPort = 25334; //端口
    private String defaultAddress = HostPortUtils.getIpAddress();


}

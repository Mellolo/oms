package com.hengtiansoft.strategy.master.controller;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RegistryController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private IRule rule;

    @GetMapping("services")
    public List<String> getEurekaServices() {
        List<String> services = new ArrayList<>();
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("oms-strategy-biz");
        for(ServiceInstance serviceInstance : serviceInstances) {
            services.add(serviceInstance.getHost()+":"+serviceInstance.getPort());
        }
        return services;
    }

    @GetMapping("instances")
    public List<String> getInstances() {
        return rule.getLoadBalancer().getReachableServers().stream().map(Server::getHostPort).collect(Collectors.toList());
    }

}

package com.hengtiansoft.strategy.bo.gateway.Router;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.*;

public class DynamicRouter extends SimpleRouteLocator implements RefreshableRouteLocator {

    /**
     * 是否启用动态路由。
     * 在配置文件中：saas.routes.dynamic.enabled，默认为false
     */
    private boolean enabled;

    public DynamicRouter(boolean enabled, String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.enabled = enabled;
    }

    /**
     * 重载路由规则
     */
    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        if (!enabled) {
            return super.locateRoutes();
        }
        Map<String, ZuulProperties.ZuulRoute> routeMap = new HashMap<>();
        // 从数据源获取路由配置
        // 先模拟几个配置
        String path = "/bo/**";
        String serviceId = "oms-bo-biz";

        // 任意一个为空，则不进行动态路由
        if (StringUtils.isBlank(path) || StringUtils.isBlank(serviceId)) {
            return super.locateRoutes();
        }
        // 生成ZuulRoute对象
        ZuulProperties.ZuulRoute zuulRoute = createZuulRoute(path, serviceId);

        routeMap.put(path, zuulRoute);

        String path1 = "/basedata/**";
        String serviceId2 = "service-basedata";
        ZuulProperties.ZuulRoute zuulRoute1 = createZuulRoute(path1, serviceId2);
        routeMap.put(path1, zuulRoute1);

        return routeMap;
    }

    /**
     * 刷新路由
     */
    @Override
    public void refresh() {
        super.doRefresh();
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 生成ZuulRoute对象
     * @param path 映射路径
     * @param serviceId 服务Id
     */
    private ZuulProperties.ZuulRoute createZuulRoute(String path, String serviceId) {
        ZuulProperties.ZuulRoute zuulRoute = new ZuulProperties.ZuulRoute();
        zuulRoute.setId(path);
        zuulRoute.setPath(path);
        zuulRoute.setServiceId(serviceId);
        return zuulRoute;
    }
}

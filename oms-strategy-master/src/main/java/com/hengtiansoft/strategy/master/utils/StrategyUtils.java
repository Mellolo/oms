package com.hengtiansoft.strategy.master.utils;

public class StrategyUtils {

    private static final ThreadLocal<String> hostPortHolder = new ThreadLocal<>();

    public static String getHostPort() {
        return hostPortHolder.get();
    }

    public static void setHostPort(String hostPort) {
        hostPortHolder.set(hostPort);
    }
}

package com.hengtiansoft.strategy.master.utils;

public class StrategyUtils {

    private static final ThreadLocal<String> strategyIdHolder = new ThreadLocal<>();

    public static String getStrategyId() {
        return strategyIdHolder.get();
    }

    public static void setStrategyId(String strategyId) {
        strategyIdHolder.set(strategyId);
    }
}

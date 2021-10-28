package com.hengtiansoft.strategy.exception;

public class StrategyException extends RuntimeException{

    public StrategyException(String strategyId, String message) {
        this(strategyId, message, null);
    }

    public StrategyException(String strategyId, String message, Throwable cause) {
        super(String.format("Strategy %s: %s", strategyId, message), cause);
    }
}

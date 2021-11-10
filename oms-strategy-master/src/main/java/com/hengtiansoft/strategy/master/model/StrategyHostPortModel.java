package com.hengtiansoft.strategy.master.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyHostPortModel {
    private String strategyId;
    private String hostPort;
}

package com.hengtiansoft.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyLogModel {
    private String id;
    private String userId;
    private String log;
}

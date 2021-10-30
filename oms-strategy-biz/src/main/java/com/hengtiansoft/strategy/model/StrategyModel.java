package com.hengtiansoft.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyModel {
    private int id;
    private String userId;
    private String code;
}

package com.hengtiansoft.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Strategy {
    private String userId;
    private String accountId;
    private String code;
}

package com.hengtiansoft.strategy.master.controller;

import com.hengtiansoft.strategy.master.feign.StrategyService;
import com.hengtiansoft.strategy.master.utils.StrategyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class LoadBalanceController {

    @Resource
    private StrategyService strategyService;

    @GetMapping("register")
    public void register(int codeId, String userId, String[] accounts)
    {
        String strategyId = UUID.randomUUID().toString();
        System.out.println(StrategyUtils.getStrategyId());
        StrategyUtils.setStrategyId(strategyId);
        strategyService.register(strategyId, codeId, userId, accounts);
        StrategyUtils.setStrategyId(null);
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println(StrategyUtils.getStrategyId());
        strategyService.unregister(strategyId);
    }

}

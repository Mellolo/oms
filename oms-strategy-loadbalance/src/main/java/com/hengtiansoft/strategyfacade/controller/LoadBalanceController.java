package com.hengtiansoft.strategyfacade.controller;

import com.hengtiansoft.strategyfacade.feign.StrategyService;
import com.hengtiansoft.strategyfacade.util.StrategyUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
public class LoadBalanceController {

    @Resource
    private StrategyService strategyService;

    @GetMapping("register")
    public void register(String codeId, String userId, String[] accounts)
    {
        String strategyId = UUID.randomUUID().toString();
        System.out.println(StrategyUtil.getStrategyId());
        StrategyUtil.setStrategyId(strategyId);
        strategyService.register(strategyId, codeId, userId, accounts);
        StrategyUtil.setStrategyId(null);
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println(StrategyUtil.getStrategyId());
        strategyService.unregister(strategyId);
        StrategyUtil.setStrategyId(strategyId);
    }

}

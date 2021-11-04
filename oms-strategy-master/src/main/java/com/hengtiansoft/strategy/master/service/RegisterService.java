package com.hengtiansoft.strategy.master.service;

import com.hengtiansoft.strategy.master.feign.StrategyService;
import com.hengtiansoft.strategy.master.utils.StrategyUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegisterService {

    @Resource
    private StrategyService strategyService;

    public boolean register(String strategyId, int codeId, String userId, String[] accounts)
    {
        System.out.println(StrategyUtils.getStrategyId());
        StrategyUtils.setStrategyId(strategyId);
        boolean res = strategyService.register(strategyId, codeId, userId, accounts);
        StrategyUtils.setStrategyId(null);
        return res;
    }

    public boolean registerDuplicate(String strategyId) {
    }

    public void unregister(String strategyId)
    {
        System.out.println(StrategyUtils.getStrategyId());
        strategyService.unregister(strategyId);
    }
}

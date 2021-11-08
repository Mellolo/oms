package com.hengtiansoft.strategy.master.service;

import com.hengtiansoft.strategy.master.feign.StrategyService;
import com.hengtiansoft.strategy.master.utils.StrategyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegisterService {

    @Resource
    private StrategyService strategyService;

    @Autowired
    RedisTemplate redisTemplate;

    public boolean register(String hostPort, String strategyId, int codeId, String userId, String[] accounts) {
        StrategyUtils.setHostPort(hostPort);
        boolean res = strategyService.register(strategyId, codeId, userId, accounts);
        StrategyUtils.setHostPort(null);
        return res;
    }

    public void unregister(String hostPort, String strategyId) {
        StrategyUtils.setHostPort(hostPort);
        strategyService.unregister(strategyId);
        StrategyUtils.setHostPort(null);
    }

    public boolean addDuplicate(String hostPort, String strategyId) {
        StrategyUtils.setHostPort(hostPort);
        boolean res = strategyService.addDuplicate(strategyId);
        StrategyUtils.setHostPort(null);
        return res;
    }

    public void removeDuplicate(String hostPort, String strategyId) {
        StrategyUtils.setHostPort(hostPort);
        strategyService.removeDuplicate(strategyId);
        StrategyUtils.setHostPort(null);
    }

    public boolean turnDuplicate2Strategy(String hostPort, String strategyId) {
        StrategyUtils.setHostPort(hostPort);
        boolean res = strategyService.turnDuplicate2Strategy(strategyId);
        StrategyUtils.setHostPort(null);
        return res;
    }
}

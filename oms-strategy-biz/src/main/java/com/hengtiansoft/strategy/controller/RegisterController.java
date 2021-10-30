package com.hengtiansoft.strategy.controller;

import com.github.dockerjava.api.DockerClient;
import com.hengtiansoft.strategy.bo.account.Account;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.feign.TradeService;
import com.hengtiansoft.strategy.model.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class RegisterController {

    @Autowired
    private StrategyEngine strategyEngine;

    @GetMapping("register")
    public boolean register(String strategyId, int codeId, String userId, String[] accounts)
    {
        System.out.println("strategyId:"+strategyId+",codeId:"+codeId+",userId:"+userId+",accounts:"+ Arrays.toString(accounts));
        RunningStrategy runningStrategy = null;
        try {
            // todo：去判定是否能注册这几个accounts
            // todo：策略信息加入到数据库

            // todo：从数据库获取策略code
            Strategy strategy = new Strategy(codeId, userId,"from hquant import *\n" +
                    "\n" +
                    "\n" +
                    "class Strategy(BaseStrategy):\n" +
                    "    def initialize(self):\n" +
                    "        self.subscribe(\"600002\")\n" +
                    "        self.subscribe(\"600003\")\n" +
                    "\n" +
                    "    def handle_tick(self, tick):\n" +
                    "        if tick.open < tick.close:\n" +
                    "            self.buy(0, '600002', 200)\n" +
                    "        elif tick.open > tick.close:\n" +
                    "            self.sell(0, '600003', 300)\n" +
                    "        Strategy.last_tick = tick\n");
            runningStrategy = new RunningStrategy(strategyId, strategy);
            for(String account : accounts) {
                runningStrategy.addAccount(new Account(account));
            }
            runningStrategy.init();

            // 加入到strategyMap
            strategyEngine.getStrategyMap().put(strategyId, runningStrategy);
            strategyEngine.getEventBus().register(runningStrategy);
            return true;
        }
        catch (Exception e) {
            if(runningStrategy!=null) {
                strategyEngine.getEventBus().unregister(runningStrategy);
                strategyEngine.getStrategyMap().remove(strategyId);
                runningStrategy.destroy();
            }
            // todo：策略信息从数据库删除
        }

        return false;
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
    }
}

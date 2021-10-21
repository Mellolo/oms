package com.hengtiansoft.strategy.controller;

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

    @Autowired
    private TradeService tradeService;

    @GetMapping("register")
    public void register(String strategyId, String codeId, String userId, String[] accounts)
    {
        //todo: 去判定是否能注册这几个accounts
        try {
            Strategy s = new Strategy(codeId, userId,"from py4j.java_gateway import JavaGateway\n\n" +
                    "def handleTick(s):\n" +
                    "    gateway = JavaGateway()\n" +
                    "    print(gateway.entry_point.matchTest(\"raw\", \"target\"))\n" +
                    "    print(s)");
            RunningStrategy rs = new RunningStrategy(strategyId, s);
            for(String account : accounts) {
                rs.addAccount(new Account(account, tradeService));
            }
            // 加入到strategyMap
            strategyEngine.getStrategyMap().put(strategyId, rs);

        }
        catch (Exception e) {

        }

        System.out.println("strategyId:"+strategyId+",codeId:"+codeId+",userId:"+userId+",accounts:"+ Arrays.toString(accounts));
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
    }
}

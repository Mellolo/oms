package com.hengtiansoft.strategy.controller;

import com.hengtiansoft.strategy.bo.account.Account;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.model.StrategyModel;
import com.hengtiansoft.strategy.service.RunningStrategyService;
import com.hengtiansoft.strategy.service.StrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
public class RegisterController {

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private RunningStrategyService runningStrategyService;

    @Autowired
    private StrategyEngine strategyEngine;

    @GetMapping("register")
    public boolean register(String strategyId, int codeId, String userId, String[] accounts)
    {
        System.out.println("strategyId:"+strategyId+",codeId:"+codeId+",userId:"+userId+",accounts:"+ Arrays.toString(accounts));
        RunningStrategy runningStrategy = null;
        Stack<String> rollbackStack = new Stack<>();
        try {
            // 1. 从数据库获取策略code
            StrategyModel strategy = strategyService.select(codeId);
            // 2. 创建runningStrategy对象
            runningStrategy = new RunningStrategy(strategyId, strategy);
            for(String account : accounts) {
                runningStrategy.addAccount(new Account(account));
            }
            // 3. runningStrategy对象插入数据库（需要回滚）
            runningStrategyService.insertRunningStrategy(
                    Stream.of(accounts).collect(Collectors.toList()),
                    runningStrategy.getRunningStrategyModel()
            );
            rollbackStack.push("deleteRunningStrategy");
            // 4. runningStrategy对象初始化（需要回滚）
            runningStrategy.init();
            rollbackStack.push("destroy");
            // 5. 运行initialize方法（需要回滚）
            runningStrategy.initialize();
            // 6. 注册到策略引擎中（需要回滚）
            runningStrategy.register(strategyEngine.getEventBus());
            rollbackStack.push("unregister");
            // 7. 加入StrategyMap（需要回滚）
            strategyEngine.getStrategyMap().put(strategyId, runningStrategy);
            rollbackStack.push("remove");
            // 8. 修改状态isUp为true（需要回滚）
            runningStrategyService.turnUp(strategyId);
            rollbackStack.push("turnDown");
            // 9. 成功添加，返回true
            return true;
        }
        catch (Exception e) {
            while(!rollbackStack.empty()) {
                switch (rollbackStack.pop()) {
                    case "turnDown": {
                        runningStrategyService.turnDown(strategyId);
                        break;
                    }
                    case "remove": {
                        strategyEngine.getStrategyMap().remove(strategyId);
                        break;
                    }
                    case "unregister": {
                        if(runningStrategy!=null) {
                            runningStrategy.unregister(strategyEngine.getEventBus());
                        }
                        break;
                    }
                    case "destroy": {
                        if(runningStrategy!=null) {
                            runningStrategy.destroy();
                        }
                        break;
                    }
                    case "deleteRunningStrategy": {
                        if(runningStrategy!=null) {
                            runningStrategyService.deleteRunningStrategy(strategyId);
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            log.error(String.format("Register abort: %s", strategyId));
        }
        return false;
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
        runningStrategyService.turnDown(strategyId);
        RunningStrategy runningStrategy = strategyEngine.getStrategyMap().remove(strategyId);
        if(runningStrategy!=null) {
            runningStrategy.unregister(strategyEngine.getEventBus());
            runningStrategy.destroy();
        }
        runningStrategyService.deleteRunningStrategy(strategyId);
    }
}

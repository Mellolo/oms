package com.hengtiansoft.strategy.controller;

import com.hengtiansoft.strategy.bo.account.Account;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.model.RunningStrategyModel;
import com.hengtiansoft.strategy.model.StrategyLogModel;
import com.hengtiansoft.strategy.model.StrategyModel;
import com.hengtiansoft.strategy.service.RunningStrategyService;
import com.hengtiansoft.strategy.service.StrategyLogService;
import com.hengtiansoft.strategy.service.StrategyService;
import com.hengtiansoft.strategy.utils.ExceptionUtils;
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
            // 5. 加入StrategyMap（需要回滚）
            // 只有加入了StrategyMap，才能使用py4j进行访问
            strategyEngine.putStrategy(strategyId, runningStrategy);
            rollbackStack.push("removeStrategy");
            // 6. 运行initialize方法（需要回滚）
            runningStrategy.initialize();
            // 7. 注册到策略引擎中（需要回滚）
            strategyEngine.registerStrategy(strategyId);
            rollbackStack.push("unregisterStrategy");
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
                    case "unregisterStrategy": {
                        if(runningStrategy!=null) {
                            strategyEngine.unregisterStrategy(strategyId);
                        }
                        break;
                    }
                    case "removeStrategy": {
                        strategyEngine.removeStrategy(strategyId);
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
            log.error(String.format("Register abort: %s, %s", strategyId, ExceptionUtils.getStackTrace(e)));
        }
        return false;
    }

    @GetMapping("register/duplicate")
    public boolean registerDuplicate(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
        RunningStrategy runningStrategy = null;
        Stack<String> rollbackStack = new Stack<>();
        try {
            // 1. 从数据库获取策略code
            RunningStrategyModel strategy = runningStrategyService.select(strategyId);
            // 2. 从数据库获取策略绑定的账户
            List<String> accounts = runningStrategyService.selectAccountBinding(strategyId);
            // 3. 创建runningStrategy对象
            runningStrategy = new RunningStrategy(strategyId, strategy);
            for(String account : accounts) {
                runningStrategy.addAccount(new Account(account));
            }
            // 4. runningStrategy对象初始化（需要回滚）
            runningStrategy.init();
            rollbackStack.push("destroy");
            // 5. 加入DuplicateMap（需要回滚）
            // 只有加入了StrategyMap，才能使用py4j进行访问
            strategyEngine.putDuplicate(strategyId, runningStrategy);
            rollbackStack.push("removeDuplicate");
            // 6. 成功添加，返回true
            return true;
        }
        catch (Exception e) {
            while(!rollbackStack.empty()) {
                switch (rollbackStack.pop()) {
                    case "removeDuplicate": {
                        strategyEngine.removeDuplicate(strategyId);
                        break;
                    }
                    case "destroy": {
                        if(runningStrategy!=null) {
                            runningStrategy.destroy();
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            log.error(String.format("Duplicate abort: %s, %s", strategyId, ExceptionUtils.getStackTrace(e)));
        }
        return false;
    }

    @GetMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("strategyId:"+strategyId);
        runningStrategyService.turnDown(strategyId);
        strategyEngine.unregisterStrategy(strategyId);
        RunningStrategy runningStrategy = strategyEngine.removeStrategy(strategyId);
        if(runningStrategy!=null) {
            runningStrategy.destroy();
        }
        runningStrategyService.deleteRunningStrategy(strategyId);
    }
}

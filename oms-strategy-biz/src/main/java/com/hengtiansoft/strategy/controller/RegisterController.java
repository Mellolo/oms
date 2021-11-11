package com.hengtiansoft.strategy.controller;

import com.hengtiansoft.strategy.bo.account.Account;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.model.RunningStrategyModel;
import com.hengtiansoft.strategy.model.StrategyModel;
import com.hengtiansoft.strategy.service.HostPortService;
import com.hengtiansoft.strategy.service.RunningStrategyService;
import com.hengtiansoft.strategy.service.StrategyService;
import com.hengtiansoft.strategy.utils.ExceptionUtils;
import com.hengtiansoft.strategy.utils.HostPortUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
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
    private HostPortService hostPortService;

    @Autowired
    private HostPortUtils hostPortUtils;

    @Autowired
    private StrategyEngine strategyEngine;

    @PostMapping("register")
    public boolean register(String strategyId, int codeId, String userId, String[] accounts)
    {
        System.out.println("register " + strategyId);
        // 双重检验
        if(!strategyEngine.contains(strategyId)) {
            ReentrantLock lock = strategyEngine.getLock(strategyId);
            lock.lock();
            if (!strategyEngine.contains(strategyId)) {
                RunningStrategy runningStrategy = null;
                Stack<String> rollbackStack = new Stack<>();
                try {
                    // 1. 从数据库获取策略code
                    StrategyModel strategy = strategyService.select(codeId);
                    // 2. 创建runningStrategy对象
                    runningStrategy = new RunningStrategy(strategyId, strategy);
                    for (String account : accounts) {
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
                    // 8. 添加到host_port列表中
                    hostPortService.insertStrategyHostport(strategyId, hostPortUtils.getHostPort());
                    rollbackStack.push("deleteStrategyHostport");
                    // 9. 成功添加，返回true
                    return true;
                } catch (Exception e) {
                    while (!rollbackStack.empty()) {
                        switch (rollbackStack.pop()) {
                            case "deleteStrategyHostport": {
                                hostPortService.deleteStrategyHostport(strategyId, hostPortUtils.getHostPort());
                                break;
                            }
                            case "unregisterStrategy": {
                                if (runningStrategy != null) {
                                    strategyEngine.unregisterStrategy(strategyId);
                                }
                                break;
                            }
                            case "removeStrategy": {
                                strategyEngine.removeStrategy(strategyId);
                                break;
                            }
                            case "destroy": {
                                if (runningStrategy != null) {
                                    runningStrategy.destroy();
                                }
                                break;
                            }
                            case "deleteRunningStrategy": {
                                if (runningStrategy != null) {
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
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            else {
                lock.unlock();
                return false;
            }
        }
        return false;
    }

    @DeleteMapping("unregister")
    public void unregister(String strategyId)
    {
        System.out.println("unregister " + strategyId);
        if(strategyEngine.containsStrategy(strategyId)) {
            ReentrantLock lock = strategyEngine.getLock(strategyId);
            lock.lock();
            if(strategyEngine.containsStrategy(strategyId)) {
                hostPortService.deleteStrategyHostport(strategyId, hostPortUtils.getHostPort());
                strategyEngine.unregisterStrategy(strategyId);
                RunningStrategy runningStrategy = strategyEngine.removeStrategy(strategyId);
                if(runningStrategy!=null) {
                    runningStrategy.destroy();
                }
                runningStrategyService.deleteRunningStrategy(strategyId);
            }
            lock.unlock();
        }
        strategyEngine.deleteLock(strategyId);
    }

    @PostMapping("add/duplicate")
    public boolean addDuplicate(String strategyId)
    {
        System.out.println("addDuplicate " + strategyId);
        // 双重检验
        if(!strategyEngine.contains(strategyId)) {
            ReentrantLock lock = strategyEngine.getLock(strategyId);
            lock.lock();
            if (!strategyEngine.contains(strategyId)) {
                RunningStrategy runningStrategy = null;
                Stack<String> rollbackStack = new Stack<>();
                try {
                    // 1. 从数据库获取策略code
                    RunningStrategyModel strategy = runningStrategyService.select(strategyId);
                    // 2. 从数据库获取策略绑定的账户
                    List<String> accounts = runningStrategyService.selectAccountBinding(strategyId);
                    // 3. 创建runningStrategy对象
                    runningStrategy = new RunningStrategy(strategyId, strategy);
                    for (String account : accounts) {
                        runningStrategy.addAccount(new Account(account));
                    }
                    // 4. runningStrategy对象初始化（需要回滚）
                    runningStrategy.init();
                    rollbackStack.push("destroy");
                    // 5. 加入DuplicateMap（需要回滚）
                    // 只有加入了StrategyMap，才能使用py4j进行访问
                    strategyEngine.putDuplicate(strategyId, runningStrategy);
                    rollbackStack.push("removeDuplicate");
                    // 6. 添加到host_port列表中
                    hostPortService.insertDuplicateHostport(strategyId, hostPortUtils.getHostPort());
                    rollbackStack.push("deleteDuplicateHostport");
                    // 7. 成功添加，返回true
                    return true;
                } catch (Exception e) {
                    while (!rollbackStack.empty()) {
                        switch (rollbackStack.pop()) {
                            case "deleteDuplicateHostport": {
                                hostPortService.deleteDuplicateHostport(strategyId, hostPortUtils.getHostPort());
                                break;
                            }
                            case "removeDuplicate": {
                                strategyEngine.removeDuplicate(strategyId);
                                break;
                            }
                            case "destroy": {
                                if (runningStrategy != null) {
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
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            else {
                lock.unlock();
                return false;
            }
        }
        return false;
    }

    @DeleteMapping("remove/duplicate")
    public void removeDuplicate(String strategyId)
    {
        System.out.println("removeDuplicate " + strategyId);
        if(strategyEngine.containsDuplicate(strategyId)) {
            ReentrantLock lock = strategyEngine.getLock(strategyId);
            lock.lock();
            if(strategyEngine.containsDuplicate(strategyId)) {
                hostPortService.deleteDuplicateHostport(strategyId, hostPortUtils.getHostPort());
                RunningStrategy runningStrategy = strategyEngine.removeDuplicate(strategyId);
                if(runningStrategy!=null) {
                    runningStrategy.destroy();
                }
            }
            lock.unlock();
        }
        strategyEngine.deleteLock(strategyId);
    }

    @PutMapping("turnDuplicate2Strategy")
    public boolean turnDuplicate2Strategy(String strategyId)
    {
        System.out.println("turnDuplicate2Strategy " + strategyId);
        if(strategyEngine.containsDuplicate(strategyId) &&
                !strategyEngine.containsStrategy(strategyId)) {
            ReentrantLock lock = strategyEngine.getLock(strategyId);
            lock.lock();
            if(strategyEngine.containsDuplicate(strategyId) &&
                    !strategyEngine.containsStrategy(strategyId)) {
                Stack<String> rollbackStack = new Stack<>();
                try {
                    // 1. 将副本转换为运行中的策略
                    strategyEngine.initialize(strategyId);
                    // 2. 将副本转换为运行中的策略
                    strategyEngine.turnDuplicate2Strategy(strategyId);
                    rollbackStack.push("turnStrategy2Duplicate");
                    // 3. 修改数据库，将数据库中副本转换为运行中的策略
                    hostPortService.updateDuplicate2Strategy(strategyId, hostPortUtils.getHostPort());
                    rollbackStack.push("abortStrategy2Duplicate");
                    // 4. 完成副本转换策略
                    return true;
                } catch (Exception e) {
                    while (!rollbackStack.empty()) {
                        switch (rollbackStack.pop()) {
                            case "abortStrategy2Duplicate": {
                                hostPortService.abortStrategy2Duplicate(strategyId, hostPortUtils.getHostPort());
                                break;
                            }
                            case "turnStrategy2Duplicate": {
                                strategyEngine.turnStrategy2Duplicate(strategyId);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    }
                    log.error(String.format("Duplicate2Strategy abort: %s, %s", strategyId, ExceptionUtils.getStackTrace(e)));
                    return false;
                } finally {
                    lock.unlock();
                }
            }
            else {
                lock.unlock();
                return false;
            }
        }
        return false;
    }

    @DeleteMapping("refresh")
    public void refresh() {
        System.out.println("refresh");
        strategyEngine.clear();
    }
}

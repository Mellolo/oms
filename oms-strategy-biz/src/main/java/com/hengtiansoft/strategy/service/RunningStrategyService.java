package com.hengtiansoft.strategy.service;

import com.hengtiansoft.strategy.mapper.RunningStrategyMapper;
import com.hengtiansoft.strategy.mapper.StrategyLogMapper;
import com.hengtiansoft.strategy.model.RunningStrategyModel;
import com.hengtiansoft.strategy.model.StrategyLogModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RunningStrategyService {

    @Autowired
    @Qualifier("runningStrategyMapper")
    RunningStrategyMapper runningStrategyMapper;

    @Autowired
    @Qualifier("strategyLogMapper")
    StrategyLogMapper strategyLogMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void insertRunningStrategy(List<String> accounts, RunningStrategyModel runningStrategyModel) {
        strategyLogMapper.insert(new StrategyLogModel(runningStrategyModel.getId(), runningStrategyModel.getUserId(), "=====STRATEGY LOG=====\n"));
        runningStrategyMapper.insertAccountBinding(accounts, runningStrategyModel.getId());
        runningStrategyMapper.insert(runningStrategyModel);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteRunningStrategy(String runningStrategyId) {
        runningStrategyMapper.delete(runningStrategyId);
        runningStrategyMapper.deleteAccountBinding(runningStrategyId);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<String> selectAccountBinding(String runningStrategyId) {
        return runningStrategyMapper.selectAccountBinding(runningStrategyId);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void turnUp(String runningStrategyId) {
        runningStrategyMapper.updateIsUp(runningStrategyId, true);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void turnDown(String runningStrategyId) {
        runningStrategyMapper.updateIsUp(runningStrategyId, false);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public RunningStrategyModel select(String id) {
        return runningStrategyMapper.select(id);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<RunningStrategyModel> selectByUserId(String userId) {
        return runningStrategyMapper.selectByUserId(userId);
    }
}

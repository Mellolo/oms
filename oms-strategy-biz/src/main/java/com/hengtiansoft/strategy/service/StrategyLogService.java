package com.hengtiansoft.strategy.service;

import com.hengtiansoft.strategy.mapper.StrategyLogMapper;
import com.hengtiansoft.strategy.model.StrategyLogModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StrategyLogService {

    @Autowired
    @Qualifier("strategyLogMapper")
    StrategyLogMapper strategyLogMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public StrategyLogModel select(String id) {
        return strategyLogMapper.select(id);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<StrategyLogModel> selectByUserId(String userId) {
        return strategyLogMapper.selectByUserId(userId);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void insert(StrategyLogModel runningStrategyModel) {
        strategyLogMapper.insert(runningStrategyModel);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void append(String runningStrategyId, String log) {
        strategyLogMapper.append(runningStrategyId, log);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void delete(String runningStrategyId) {
        strategyLogMapper.delete(runningStrategyId);
    }

}

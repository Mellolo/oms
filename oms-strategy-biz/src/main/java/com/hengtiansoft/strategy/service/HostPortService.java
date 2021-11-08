package com.hengtiansoft.strategy.service;

import com.hengtiansoft.strategy.mapper.HostPortMapper;
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
public class HostPortService {

    @Autowired
    @Qualifier("hostPortMapper")
    private HostPortMapper hostPortMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void insertStrategyHostport(String strategyId, String hostPort) {
        hostPortMapper.insertStrategyHostport(strategyId, hostPort);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteStrategyHostport(String strategyId, String hostPort) {
        hostPortMapper.deleteStrategyHostport(strategyId, hostPort);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void insertDuplicateHostport(String strategyId, String hostPort) {
        hostPortMapper.insertDuplicateHostport(strategyId, hostPort);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteDuplicateHostport(String strategyId, String hostPort) {
        hostPortMapper.deleteDuplicateHostport(strategyId, hostPort);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateDuplicate2Strategy(String strategyId, String hostPort) {
        hostPortMapper.insertStrategyHostport(strategyId, hostPort);
        hostPortMapper.deleteDuplicateHostport(strategyId, hostPort);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void abortStrategy2Duplicate(String strategyId, String hostPort) {
        hostPortMapper.insertDuplicateHostport(strategyId, hostPort);
        hostPortMapper.deleteStrategyHostport(strategyId, hostPort);
    }
}

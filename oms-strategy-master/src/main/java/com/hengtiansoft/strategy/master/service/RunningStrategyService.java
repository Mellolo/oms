package com.hengtiansoft.strategy.master.service;

import com.hengtiansoft.strategy.master.mapper.RunningStrategyMapper;
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

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public boolean isUp(String runningStrategyId) {
        Boolean res = runningStrategyMapper.isUp(runningStrategyId);
        if(res==null) {
            return false;
        }
        else {
            return res;
        }
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void turnUp(String runningStrategyId) {
        runningStrategyMapper.updateIsUp(runningStrategyId, true);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void turnDown(String runningStrategyId) {
        runningStrategyMapper.updateIsUp(runningStrategyId, false);
    }

}

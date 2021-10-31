package com.hengtiansoft.strategy.service;

import com.hengtiansoft.strategy.mapper.StrategyMapper;
import com.hengtiansoft.strategy.model.StrategyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StrategyService {

    @Autowired
    @Qualifier("strategyMapper")
    StrategyMapper strategyMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public StrategyModel select(int id) {
        return strategyMapper.select(id);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<StrategyModel> selectByUserId(String userId) {
        return strategyMapper.selectByUserId(userId);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void insert(StrategyModel strategyModel) {
        strategyMapper.insert(strategyModel);
    }

}

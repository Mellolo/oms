package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.StrategyModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyMapper {

    StrategyModel select(int id);

    List<StrategyModel> selectByUserId(String userId);

    void insert(StrategyModel strategyModel);

    void delete(int id);
}

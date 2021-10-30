package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.StrategyModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StrategyMapper {

    StrategyModel select(int id);

}

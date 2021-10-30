package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.RunningStrategyModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RunningStrategyMapper {

    RunningStrategyModel select(int id);

}

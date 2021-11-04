package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.StrategyLogModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StrategyLogMapper {

    StrategyLogModel select(String userId);

    List<StrategyLogModel> selectByUserId(String userId);

    void insert(StrategyLogModel strategyLogModel);

    void append(@Param("id") String id, @Param("log") String log);

    void delete( String id);
}

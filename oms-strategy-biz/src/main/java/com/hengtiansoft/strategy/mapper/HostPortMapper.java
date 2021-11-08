package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.StrategyModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HostPortMapper {

    void insertStrategyHostport(@Param("strategyId") String strategyId, @Param("hostPort") String hostPort);

    void insertDuplicateHostport(@Param("strategyId") String strategyId, @Param("hostPort") String hostPort);

    void deleteStrategyHostport(@Param("strategyId") String strategyId, @Param("hostPort") String hostPort);

    void deleteDuplicateHostport(@Param("strategyId") String strategyId, @Param("hostPort") String hostPort);
}

package com.hengtiansoft.strategy.master.mapper;

import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import com.hengtiansoft.strategy.master.model.StrategyHostPortModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface HostPortMapper {

    List<String> selectStrategyByHostPort(@Param("hostPorts") Set<String> hostPorts);

    StrategyHostPortModel selectStrategyHostPortById(String strategyId);

    List<StrategyHostPortModel> selectDuplicateHostPortById(@Param("strategyIds") List<String> strategyIds);

    List<HostPortCountModel> selectStrategyHostPortCountByHostPort(@Param("hostPorts") Set<String> hostPorts);

    List<HostPortCountModel> selectDuplicateHostPortCountByHostPort(@Param("hostPorts") Set<String> hostPorts);

    void deleteStrategyHostPort(@Param("hostPorts") Set<String> hostPorts);

    void deleteDuplicateHostPort(@Param("hostPorts") Set<String> hostPorts);
}

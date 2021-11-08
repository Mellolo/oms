package com.hengtiansoft.strategy.master.mapper;

import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HostPortMapper {

    List<HostPortCountModel> selectStrategyHostport(@Param("hostPorts") List<String> hostPorts);

    List<HostPortCountModel> selectDuplicateHostport(@Param("hostPorts") List<String> hostPorts);

    void deleteStrategyHostport(@Param("hostPorts") List<String> hostPorts);

    void deleteDuplicateHostport(@Param("hostPorts") List<String> hostPorts);
}

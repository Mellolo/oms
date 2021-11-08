package com.hengtiansoft.strategy.master.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface RunningStrategyMapper {

    boolean isUp(String id);

    void updateIsUp(@Param("id") String id, @Param("isUp") boolean isUp);

}

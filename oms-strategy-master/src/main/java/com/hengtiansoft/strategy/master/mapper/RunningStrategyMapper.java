package com.hengtiansoft.strategy.master.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface RunningStrategyMapper {

    List<String> selectIdAll();

    Boolean isUp(String id);

    void updateIsUp(@Param("id") String id, @Param("isUp") boolean isUp);

    void delete(String id);

    void deleteAccountBinding(String runningStrategyId);
}

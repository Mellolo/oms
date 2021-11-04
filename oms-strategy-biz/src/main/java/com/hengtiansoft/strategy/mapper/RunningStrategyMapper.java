package com.hengtiansoft.strategy.mapper;

import com.hengtiansoft.strategy.model.RunningStrategyModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RunningStrategyMapper {

    RunningStrategyModel select(String id);

    List<RunningStrategyModel> selectByUserId(String userId);

    boolean isUp(String id);

    void insert(RunningStrategyModel runningStrategyModel);

    void delete(String id);

    void updateIsUp(@Param("id") String id, @Param("isUp") boolean isUp);

    List<String> selectAccountBinding(String runningStrategyId);

    void insertAccountBinding(@Param("accountIds") List<String> accountIds, @Param("runningStrategyId") String runningStrategyId);

    void deleteAccountBinding(String runningStrategyId);

}

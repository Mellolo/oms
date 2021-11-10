package com.hengtiansoft.strategy.master.service;

import com.hengtiansoft.strategy.master.mapper.HostPortMapper;
import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import com.hengtiansoft.strategy.master.model.StrategyHostPortModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HostPortService {

    @Autowired
    @Qualifier("hostPortMapper")
    private HostPortMapper hostPortMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<String> selectStrategyByHostPort(Set<String> hostPorts) {
        return hostPortMapper.selectStrategyByHostPort(hostPorts);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public StrategyHostPortModel selectStrategyHostPortById(String strategyId) {
        return hostPortMapper.selectStrategyHostPortById(strategyId);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<StrategyHostPortModel> selectDuplicateHostPortById(List<String> strategyIds) {
        return hostPortMapper.selectDuplicateHostPortById(strategyIds);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<HostPortCountModel> selectStrategyHostPortCountByHostPort(Set<String> hostPorts) {
        List<HostPortCountModel> hostPortCountModels = hostPortMapper.selectStrategyHostPortCountByHostPort(hostPorts);
        fillUp(hostPortCountModels, hostPorts);
        return hostPortCountModels;
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<HostPortCountModel> selectDuplicateHostPortCountByHostPort(Set<String> hostPorts) {
        List<HostPortCountModel> hostPortCountModels = hostPortMapper.selectDuplicateHostPortCountByHostPort(hostPorts);
        fillUp(hostPortCountModels, hostPorts);
        return hostPortCountModels;
    }

    private void fillUp(List<HostPortCountModel> hostPortCountModels, Set<String> hostPorts) {
        Set<String> setNotEmpty = hostPortCountModels.stream().map(HostPortCountModel::getHostPort).collect(Collectors.toSet());
        Set<String> setEmpty = new HashSet<>(hostPorts);
        setEmpty.removeAll(setNotEmpty);
        hostPortCountModels.addAll(
                setEmpty.stream().map(h->new HostPortCountModel(h,0)).collect(Collectors.toList())
        );
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteHostPort(Set<String> hostPorts) {
        hostPortMapper.deleteStrategyHostPort(hostPorts);
        hostPortMapper.deleteDuplicateHostPort(hostPorts);
    }
}

package com.hengtiansoft.strategy.master.service;

import com.hengtiansoft.strategy.master.mapper.HostPortMapper;
import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class HostPortService {

    @Autowired
    @Qualifier("hostPortMapper")
    private HostPortMapper hostPortMapper;

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteHostPort(List<String> hostPorts) {
        hostPortMapper.deleteStrategyHostport(hostPorts);
        hostPortMapper.deleteDuplicateHostport(hostPorts);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<HostPortCountModel> selectStrategyHostport(List<String> hostPorts) {
        return hostPortMapper.selectStrategyHostport(hostPorts);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public List<HostPortCountModel> selectDuplicateHostport(List<String> hostPorts) {
        return hostPortMapper.selectDuplicateHostport(hostPorts);
    }
}

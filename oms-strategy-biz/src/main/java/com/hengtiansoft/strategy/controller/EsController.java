package com.hengtiansoft.strategy.controller;

import com.hengtiansoft.strategy.dao.EsTemplate;
import com.hengtiansoft.strategy.model.Strategy;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py4j.GatewayServer;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EsController {

    @Autowired
    private EsTemplate esteamplate;

    @Autowired
    private GatewayServer gatewayServer;

    @RequestMapping("/add/strategy")
    public BulkResponse addStrategy(String userId, String accountId, String code) {
        Strategy s = new Strategy(userId, accountId, code);
        return esteamplate.bulkInsert(new ArrayList<Strategy>(){{ add(s); }});
    }

    @GetMapping("search")
    public List<Strategy> search(String detail) {
        return esteamplate.search();
    }
}

package com.hengtiansoft.trade.controller;

import com.hengtiansoft.trade.feign.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    StrategyService strategyService;

    @RequestMapping(value = "test/register" , method = RequestMethod.GET)
    public void testRegister()
    {
        System.out.println(
                strategyService.register(112,"123",new String[]{"a","b"})
        );
    }

    @RequestMapping(value = "test/unregister/{strategyId}" , method = RequestMethod.GET)
    public void testUnregister(@PathVariable String strategyId)
    {
        System.out.println(
                strategyService.unregister(strategyId)
        );
    }
}

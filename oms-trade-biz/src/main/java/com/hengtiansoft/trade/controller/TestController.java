package com.hengtiansoft.trade.controller;

import com.hengtiansoft.trade.feign.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
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
        strategyService.register(111,"123",new String[]{"a","b"});
    }
}

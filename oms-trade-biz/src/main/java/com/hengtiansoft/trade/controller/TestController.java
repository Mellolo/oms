package com.hengtiansoft.trade.controller;

import com.hengtiansoft.trade.component.TickProducer;
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

    @Autowired
    TickProducer tickProducer;

    @RequestMapping(value = "test/register/{accountId}" , method = RequestMethod.GET)
    public void testRegister(@PathVariable String accountId)
    {
        System.out.println(
                strategyService.register(111,"123",new String[]{accountId})
        );
    }

    @RequestMapping(value = "test/initial/{accountId}" , method = RequestMethod.GET)
    public void testDownRegister(@PathVariable String accountId)
    {
        System.out.println(
                strategyService.register(112,"123",new String[]{accountId})
        );
    }

    @RequestMapping(value = "test/handle/{accountId}" , method = RequestMethod.GET)
    public void testDownHandleTick(@PathVariable String accountId)
    {
        System.out.println(
                strategyService.register(113,"123",new String[]{accountId})
        );
    }

    @RequestMapping(value = "test/unregister/{strategyId}" , method = RequestMethod.GET)
    public void testUnregister(@PathVariable String strategyId)
    {
        System.out.println(
                strategyService.unregister(strategyId)
        );
    }


    @RequestMapping(value = "test/tick/{code}" , method = RequestMethod.GET)
    public void testTick(@PathVariable String code)
    {
        tickProducer.produceTick(code);
    }
}

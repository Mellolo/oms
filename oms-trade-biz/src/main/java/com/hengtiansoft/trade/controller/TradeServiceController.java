package com.hengtiansoft.trade.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeServiceController {

    @RequestMapping(value = "sell" , method = RequestMethod.GET)
    public boolean sell(String accountId, String security, int volume)
    {
        System.out.println("Account("+accountId+") sell ("+security+") volume ("+volume+")");
        return true;
    }

    @RequestMapping(value = "buy" , method = RequestMethod.GET)
    boolean buy(String accountId, String security, int volume)
    {
        System.out.println("Account("+accountId+") buy ("+security+") volume ("+volume+")");
        return true;
    }


    @RequestMapping(value = "position" , method = RequestMethod.GET)
    int getPosition(String accountId, String security)
    {
        return 300;
    }
}

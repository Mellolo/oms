package com.hengtiansoft.strategy.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oms-trade-biz")
public interface TradeService {

    @RequestMapping(value = "buy" , method = RequestMethod.GET)
    boolean buy(@RequestParam("accountId") String accountId, @RequestParam("security") String security, @RequestParam("volume") int volume);

    @RequestMapping(value = "sell" , method = RequestMethod.GET)
    boolean sell(@RequestParam("accountId") String accountId, @RequestParam("security") String security, @RequestParam("volume") int volume);

    @RequestMapping(value = "position" , method = RequestMethod.GET)
    int getPosition(@RequestParam("accountId") String accountId, @RequestParam("security") String security);
}

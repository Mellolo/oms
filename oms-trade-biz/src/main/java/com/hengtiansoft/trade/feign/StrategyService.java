package com.hengtiansoft.trade.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oms-strategy-master")
public interface StrategyService {

    @RequestMapping(value = "register" , method = RequestMethod.GET)
    public void register(@RequestParam("codeId") int codeId, @RequestParam("userId") String userId,  @RequestParam("accounts") String[] accounts);

    @RequestMapping(value = "unregister" , method = RequestMethod.GET)
    public void unregister(@RequestParam("strategyId") String strategyId);
}

package com.hengtiansoft.strategyfacade.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oms-strategy-biz")
public interface StrategyService {

    @RequestMapping(value = "register" , method = RequestMethod.GET)
    public void register(@RequestParam("strategyId") String strategyId, @RequestParam("codeId") int codeId, @RequestParam("userId") String userId,  @RequestParam("accounts") String[] accounts);

    @RequestMapping(value = "unregister" , method = RequestMethod.GET)
    public void unregister(@RequestParam("strategyId") String strategyId);

}

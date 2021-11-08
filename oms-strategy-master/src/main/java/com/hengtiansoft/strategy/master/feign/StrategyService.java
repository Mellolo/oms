package com.hengtiansoft.strategy.master.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "oms-strategy-biz")
public interface StrategyService {

    @RequestMapping(value = "register" , method = RequestMethod.POST)
    public boolean register(@RequestParam("strategyId") String strategyId, @RequestParam("codeId") int codeId, @RequestParam("userId") String userId,  @RequestParam("accounts") String[] accounts);

    @RequestMapping(value = "unregister" , method = RequestMethod.DELETE)
    public void unregister(@RequestParam("strategyId") String strategyId);

    @RequestMapping(value = "add/duplicate" , method = RequestMethod.POST)
    public boolean addDuplicate(@RequestParam("strategyId") String strategyId);

    @RequestMapping(value = "remove/duplicate" , method = RequestMethod.DELETE)
    public void removeDuplicate(@RequestParam("strategyId") String strategyId);

    @RequestMapping(value = "turnDuplicate2Strategy" , method = RequestMethod.PUT)
    public boolean turnDuplicate2Strategy(@RequestParam("strategyId") String strategyId);

    @RequestMapping(value = "refresh" , method = RequestMethod.DELETE)
    public void refresh();

}

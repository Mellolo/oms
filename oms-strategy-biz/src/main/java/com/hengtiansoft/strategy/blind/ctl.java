package com.hengtiansoft.strategy.blind;

import com.hengtiansoft.eventbus.BaseEvent;
import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.feign.TradeService;
import com.hengtiansoft.strategy.model.Strategy;
import com.hengtiansoft.strategy.bo.event.TickEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ctl {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private EventBus eventBus;


    //@GetMapping("unregister")
    public void unregister(String StrategyId)
    {
    }

    //@GetMapping("register")
    public void register(String StrategyId, String userId, String[] accounts)
    {
        System.out.println("register");
        Strategy s = new Strategy("123","asd","from py4j.java_gateway import JavaGateway\n" +
                "\n" +
                "def handleTick(s):\n" +
                "    gateway = JavaGateway()\n" +
                "    print(gateway.entry_point.matchTest(\"raw\", \"target\"))\n" +
                "    print(s)");
        RunningStrategys rs = new RunningStrategys(s);
        rs.init();
        rs.addEventListened(TickEvent.class,"000001.XSHE");
        rs.register(eventBus);
    }

    @GetMapping("post")
    public void post()
    {
        BaseEvent event1 = new TickEvent("000001.XSHE", "000001.XSHE", new Date(2020,0,31,10,30,30),1,1,1,1,1,1);
        eventBus.post(event1);
    }

    @GetMapping("trade")
    public void trade()
    {
        System.out.println(tradeService.getPosition("3", "000001"));
    }
}

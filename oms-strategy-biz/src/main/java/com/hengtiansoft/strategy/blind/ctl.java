package com.hengtiansoft.strategy.blind;

import com.hengtiansoft.eventbus.BaseEvent;
import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.model.Strategy;
import com.hengtiansoft.strategy.strategy.Accounts;
import com.hengtiansoft.strategy.strategy.event.TickEvent;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ctl {

    @Autowired
    private EventBus eventBus;

    @Autowired
    Accounts accounts;

    @GetMapping("accounts")
    public int accounts()
    {
        accounts.addAccount("a01");
        accounts.addAccount("a02");
        accounts.addAccount("a03");

        accounts.getAccount("a01").buy("300081",700);
        accounts.getAccount("a02").sell("300082",700);
        return accounts.getAccount("a03").getPosition("300083");
    }

    @GetMapping("register")
    public void register()
    {
        Strategy s = new Strategy("123","asd","from py4j.java_gateway import JavaGateway\n" +
                "\n" +
                "def handleTick(s):\n" +
                "    gateway = JavaGateway()\n" +
                "    print(gateway.entry_point.matchTest(\"raw\", \"target\"))\n" +
                "    print(s)");
        RunningStrategy rs = new RunningStrategy(s);
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
}

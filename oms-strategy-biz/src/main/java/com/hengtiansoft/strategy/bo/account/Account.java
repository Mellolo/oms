package com.hengtiansoft.strategy.bo.account;

import com.hengtiansoft.strategy.component.utils.ApplicationContextUtils;
import com.hengtiansoft.strategy.exception.StrategyException;
import com.hengtiansoft.strategy.feign.TradeService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class Account {

    private TradeService tradeService;

    private String id; //所有调用交易模块所需要的账户相关信息和数据

    public Account(String id) //所有调用交易模块所需要的账户相关信息和数据
    {
        this.id = id;
        ApplicationContext ac = ApplicationContextUtils.getApplicationContext();
        if(ac!=null) {
            this.tradeService = ac.getBean(TradeService.class);
        }
    }

    public boolean buy(String security, int volume)
    {
        return tradeService.buy(id, security, volume);
    }

    public boolean sell(String security, int volume)
    {
        return tradeService.sell(id, security, volume);
    }

    public int getPosition(String security)
    {
        return tradeService.getPosition(id, security);
    }
}

package com.hengtiansoft.strategy.strategy;

import com.google.common.base.MoreObjects;
import com.hengtiansoft.strategy.feign.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Accounts
{
    @Autowired
    private TradeService tradeService;

    private Map<String, Account> accountMap = new ConcurrentHashMap<>();

    public Account getAccount(String accountId)
    {
        return accountMap.get(accountId);
    }

    public void addAccount(String accountId)
    {
        accountMap.putIfAbsent(accountId, new Account(accountId, tradeService));
    }

    public void updateAccount(String accountId)
    {
        accountMap.put(accountId, new Account(accountId, tradeService));
    }

}


package com.hengtiansoft.strategy.strategy;


import com.hengtiansoft.eventbus.BaseListener;

import java.util.ArrayList;
import java.util.List;

public class BaseStrategy extends BaseListener
{
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account){
        accounts.add(account);
    }

    final protected boolean buy(int index, String security, int volume)
    {
        return accounts.get(index).buy(security,volume);
    }

    final protected boolean sell(int index, String security, int volume)
    {
        return accounts.get(index).sell(security,volume);
    }

    final protected int getPosition(int index, String security, int volume)
    {
        return accounts.get(index).getPosition(security);
    }
}

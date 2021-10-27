package com.hengtiansoft.strategy.bo.strategy;


import com.hengtiansoft.eventbus.BaseListener;
import com.hengtiansoft.strategy.bo.account.Account;

import java.util.ArrayList;
import java.util.List;

public class BaseStrategy extends BaseListener
{
    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public final int accountNum() {
        return accounts.size();
    }

    public final boolean buy(int index, String security, int volume) {
        return accounts.get(index).buy(security, volume);
    }

    public final boolean sell(int index, String security, int volume) {
        return accounts.get(index).sell(security, volume);
    }

    public final int getPosition(int index, String security) {
        return accounts.get(index).getPosition(security);
    }
}

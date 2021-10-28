package com.hengtiansoft.strategy.bo.event;

import com.hengtiansoft.eventbus.BaseEvent;

import java.util.Date;

public class TickEvent extends BaseEvent {

    private String code;
    private Date date;
    private double current;
    private double open;
    private double high;
    private double low;
    private double volume;
    private double money;

    public TickEvent(String tag, String code, Date date, double current, double open, double high, double low, double volume, double money) {
        super(tag);
        this.code = code;
        this.date = date;
        this.current = current;
        this.open = open;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.money = money;
    }

    public String getCode() {
        return code;
    }

    public Date getDate() {
        return date;
    }

    public double getCurrent() {
        return current;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getVolume() {
        return volume;
    }

    public double getMoney() {
        return money;
    }

    @Override
    public String toString() {
        return "code=" + code +
                "&date=" + date +
                "&current=" + current +
                "&open=" + open +
                "&high=" + high +
                "&low=" + low +
                "&volume=" + volume +
                "&money=" + money;
    }
}

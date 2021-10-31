package com.hengtiansoft.strategy.bo.strategy.event;


import com.hengtiansoft.eventbus.BaseEvent;

import java.util.Date;

public class BarEvent extends BaseEvent {

    private String code;
    private Date date;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private double money;

    public BarEvent(String tag, String code, Date date, double open, double high, double low, double close, double volume, double money) {
        super(tag);
        this.code = code;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.money = money;
    }

    public String getCode() {
        return code;
    }

    public Date getDate() {
        return date;
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

    public double getClose() {
        return close;
    }

    public double getVolume() {
        return volume;
    }

    public double getMoney() {
        return money;
    }
}

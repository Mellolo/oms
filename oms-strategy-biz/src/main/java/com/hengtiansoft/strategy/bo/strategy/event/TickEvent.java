package com.hengtiansoft.strategy.bo.strategy.event;

import com.alibaba.fastjson.JSONObject;
import com.hengtiansoft.eventbus.BaseEvent;

import java.util.Date;
import java.util.Map;

public class TickEvent extends BaseEvent {

    private JSONObject params;

    public TickEvent(String tag, JSONObject params) {
        super(tag);
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String,Object> entry: params.entrySet()) {
            if(first) {
                first = false;
            }
            else {
                stringBuilder.append("&");
            }
            stringBuilder.append(String.format("%s=%s",entry.getKey(), entry.getValue().toString()));
        }
        return stringBuilder.toString();
    }
}

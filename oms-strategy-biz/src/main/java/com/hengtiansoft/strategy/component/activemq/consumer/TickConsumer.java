package com.hengtiansoft.strategy.component.activemq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.event.TickEvent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Component
public class TickConsumer {

    @Autowired
    StrategyEngine strategyEngine;

    @JmsListener(destination = "tick")
    public void receive(TextMessage textMessage) throws JMSException {
        System.out.println("订阅者消费消息：" + textMessage.getText());
        JSONObject jsonObject = JSON.parseObject(textMessage.getText());
        String tag = jsonObject.getString("code");
        if(StringUtils.isNotBlank(tag)) {
            strategyEngine.post(new TickEvent(tag, jsonObject));
        }
    }
}

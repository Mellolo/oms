package com.hengtiansoft.trade.component;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

@Component
public class TickProducer {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    private Topic topic;

    //@Scheduled(fixedDelay = 3000)//定时发布消息
    public void produceTick(String code) {
        // todo: producer
        Map<String, Object> tickParams = new HashMap<>();
        tickParams.put("code", code);
        tickParams.put("millis", System.currentTimeMillis());
        tickParams.put("open", 100.01);
        tickParams.put("close", 100.99);
        tickParams.put("high", 101.01);
        tickParams.put("low", 100.01);
        tickParams.put("volume", 10000);
        tickParams.put("money", 1005000);

        jmsMessagingTemplate.convertAndSend(topic, JSON.toJSONString(tickParams));
    }
}

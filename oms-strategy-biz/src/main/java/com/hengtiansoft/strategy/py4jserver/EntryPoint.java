package com.hengtiansoft.strategy.py4jserver;

import com.hengtiansoft.strategy.dao.EsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntryPoint {

    @Autowired
    EsTemplate esTemplate;

    public String matchTest(String a, String b)
    {
        System.out.println(esTemplate);
        //System.out.println(esTemplate.search());
        return esTemplate.search().toString();
    }
}
